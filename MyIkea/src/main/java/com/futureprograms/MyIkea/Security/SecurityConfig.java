package com.futureprograms.MyIkea.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.futureprograms.MyIkea.Repositories.Auth.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;

import java.util.List;

@Configuration
public class SecurityConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);
    private final UserRepository userRepository;

    @Value("${app.frontend.allowed-origins:http://localhost,http://localhost:5173}")
    private String allowedFrontendOrigins;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter
    ) throws Exception {
        AuthenticationEntryPoint apiAuthenticationEntryPoint = (request, response, authException) ->
            writeApiError(response, HttpServletResponse.SC_UNAUTHORIZED, "unauthorized",
                "Authentication is required to access this resource", request.getRequestURI());

        AccessDeniedHandler apiAccessDeniedHandler = (request, response, accessDeniedException) ->
            writeApiError(response, HttpServletResponse.SC_FORBIDDEN, "forbidden",
                "You do not have permission to access this resource", request.getRequestURI());

        AuthenticationEntryPoint loginEntryPoint = new LoginUrlAuthenticationEntryPoint("/login");

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/register", "/registration-success", "/login", "/error").permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                    .requestMatchers("/api/v1/public/**").permitAll()
                    .requestMatchers("/api/v1/auth/login", "/api/v1/auth/logout", "/api/v1/auth/refresh", "/api/v1/auth/register").permitAll()
                    .requestMatchers("/api/v1/users", "/api/v1/users/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/profile", "/api/v1/profile/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                    .requestMatchers("/api/v1/products", "/api/v1/products/**").hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers("/api/v1/cart", "/api/v1/cart/**", "/api/v1/orders", "/api/v1/orders/**")
                        .hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers("/products/create", "/products/create/**").hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers("/products/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                    .requestMatchers("/cart/**").hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers("/orders/**").hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers("/profile/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                    .requestMatchers("/users/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                    .loginPage("/login")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .successHandler((request, response, authentication) -> {
                        jwtService.attachAuthCookies(response, authentication);
                        response.sendRedirect("/products");
                    })
                    .failureUrl("/login?error")
                    .permitAll()
                )
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .addLogoutHandler((request, response, authentication) -> jwtService.clearAuthCookies(response))
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .permitAll()
                )
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> {
                        if (isApiRequest(request)) {
                            apiAuthenticationEntryPoint.commence(request, response, authException);
                            return;
                        }
                        loginEntryPoint.commence(request, response, authException);
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        if (isApiRequest(request)) {
                            apiAccessDeniedHandler.handle(request, response, accessDeniedException);
                            return;
                        }
                        response.sendRedirect("/error");
                    })
                )
                .addFilterBefore(jwtCookieAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return usernameOrEmail -> userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> {
                    LOGGER.warn("Intento de login con usuario no encontrado: {}", usernameOrEmail);
                    return new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "Usuario no encontrado: " + usernameOrEmail);
                });
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = List.of(allowedFrontendOrigins.split(","))
            .stream()
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void writeApiError(HttpServletResponse response, int status, String error, String message, String path)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("X-Error-Code", Integer.toString(status));

        String json = "{"
                + "\"timestamp\":\"" + Instant.now() + "\","
                + "\"status\":" + status + ","
                + "\"statusText\":\"" + HttpStatus.valueOf(status).getReasonPhrase() + "\","
                + "\"error\":\"" + escapeJson(error) + "\","
                + "\"message\":\"" + escapeJson(message) + "\","
                + "\"path\":\"" + escapeJson(path) + "\""
                + "}";

        response.getWriter().write(json);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI() != null && request.getRequestURI().startsWith("/api/");
    }
}