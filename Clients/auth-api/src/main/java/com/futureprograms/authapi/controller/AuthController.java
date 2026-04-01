package com.futureprograms.authapi.controller;

import com.futureprograms.authapi.config.JwtProvider;
import com.futureprograms.authapi.dto.AuthResponse;
import com.futureprograms.authapi.dto.ChangeRoleRequest;
import com.futureprograms.authapi.dto.LoginRequest;
import com.futureprograms.authapi.dto.UserDto;
import com.futureprograms.authapi.entity.User;
import com.futureprograms.authapi.enums.Role;
import com.futureprograms.authapi.repository.UserRepository;
import com.futureprograms.authapi.service.ImageService;
import com.futureprograms.authapi.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final ImageService imageService;
    private final UserRepository userRepository;

    @Value("${app.frontend.login-url:http://localhost/login}")
    private String frontendLoginUrl;

    @Value("${app.frontend.register-url:http://localhost/register}")
    private String frontendRegisterUrl;

    /**
     * Login del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        try {
            log.info("Intento de login para email: {}", request.getEmail());

            // Validar credenciales
            if (!userService.validateCredentials(request.getEmail(), request.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        AuthResponse.error("Email o contraseña incorrectos")
                );
            }

            // Obtener usuario
            User user = userService.getUserByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Generar JWT
            String token = jwtProvider.generateToken(user.getEmail());

            // Configurar cookie con JWT
            addAuthCookie(response, token);

            UserDto userDto = UserDto.fromEntity(user);

            log.info("Login exitoso para usuario: {} con rol: {}", user.getEmail(), user.getRole().getDisplayName());

            return ResponseEntity.ok().body(
                    AuthResponse.success(
                            "Login exitoso",
                            token,
                            userDto
                    )
            );
        } catch (Exception e) {
            log.error("Error en login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthResponse.error(e.getMessage())
            );
        }
    }

    private void addAuthCookie(HttpServletResponse response, String token) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("auth_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Cambiar a false para HTTP local, true para HTTPS en producción
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 24 horas
        response.addCookie(cookie);
        // Usar SameSite=Lax para permitir cookies en requests CORS
        response.addHeader("Set-Cookie", "auth_token=" + token + "; Path=/; Max-Age=86400; HttpOnly; SameSite=Lax");
    }

    /**
     * Verifica el email del usuario
     */
    @GetMapping("/verify/{token}")
    public void verifyEmail(@PathVariable String token, HttpServletResponse response) throws IOException {
        try {
            log.info("Verificación de email con token");

            userService.verifyEmail(token);
            response.sendRedirect(frontendLoginUrl + "?verified=1");
        } catch (Exception e) {
            log.error("Error en verificación: {}", e.getMessage());
            response.sendRedirect(frontendRegisterUrl + "?verification=failed");
        }
    }

    /**
     * Obtiene el perfil del usuario autenticado
     */
    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> getProfile(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        AuthResponse.error("No autenticado")
                );
            }

            String email = authentication.getName();
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            UserDto userDto = UserDto.fromEntity(user);

            return ResponseEntity.ok().body(
                    AuthResponse.success("Perfil obtenido", null, userDto)
            );
        } catch (Exception e) {
            log.error("Error al obtener perfil: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Refresca el token JWT
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            Authentication authentication,
            HttpServletResponse response
    ) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        AuthResponse.error("No autenticado")
                );
            }

            String email = authentication.getName();

            // Generar nuevo token
            String newToken = jwtProvider.generateToken(email);

            // Configurar cookie con nuevo JWT
            addAuthCookie(response, newToken);

            return ResponseEntity.ok().body(
                    AuthResponse.builder()
                            .success(true)
                            .message("Token refrescado")
                            .token(newToken)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error al refrescar token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Logout del usuario
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletResponse response) {
        try {
            // Eliminar cookie
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("auth_token", "");
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            response.addHeader("Set-Cookie", "SameSite=Strict");

            log.info("Logout exitoso");

            return ResponseEntity.ok().body(
                    AuthResponse.success("Logout exitoso", null, null)
            );
        } catch (Exception e) {
            log.error("Error en logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Elimina la cuenta del usuario (requiere autenticación)
     */
    @PostMapping("/delete-account")
    public ResponseEntity<AuthResponse> deleteAccount(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        AuthResponse.error("No autenticado")
                );
            }

            String email = authentication.getName();
            Optional<User> userOpt = userService.getUserByEmail(email);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        AuthResponse.error("Usuario no encontrado")
                );
            }

            User user = userOpt.get();
            
            // Intentar eliminar imagen si existe y no es protegida
            if (user.getProfileImg() != null && !user.getProfileImg().isEmpty()) {
                try {
                    if (!imageService.isProtectedImage(user.getProfileImg())) {
                        imageService.deleteImage(user.getProfileImg());
                        log.info("Imagen del usuario eliminada: {}", user.getProfileImg());
                    } else {
                        log.info("No se elimina imagen protegida para usuario: {}", email);
                    }
                } catch (Exception e) {
                    log.warn("No se pudo eliminar imagen del usuario: {}", e.getMessage());
                    // Continuar con la eliminación de la cuenta
                }
            }

            // Eliminar usuario
            userRepository.delete(user);
            log.info("Cuenta eliminada para usuario: {}", email);

            return ResponseEntity.ok().body(
                    AuthResponse.success("Cuenta eliminada exitosamente", null, null)
            );
        } catch (Exception e) {
            log.error("Error al eliminar cuenta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Validar si el token es válido
     */
    @PostMapping("/validate-token")
    public ResponseEntity<AuthResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        AuthResponse.error("Token no proporcionado")
                );
            }

            String token = authHeader.substring(7);

            if (jwtProvider.isTokenValid(token) && !jwtProvider.isTokenExpired(token)) {
                String email = jwtProvider.getEmailFromToken(token);
                Optional<User> userOpt = userService.getUserByEmail(email);
                
                if (userOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                            AuthResponse.error("Usuario no encontrado")
                    );
                }
                
                UserDto userDto = UserDto.fromEntity(userOpt.get());
                return ResponseEntity.ok().body(
                        AuthResponse.builder()
                                .success(true)
                                .message("Token válido")
                                .user(userDto)
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        AuthResponse.error("Token inválido o expirado")
                );
            }
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Cambiar el rol de un usuario (requiere autenticación y rol ADMIN)
     */
    @PostMapping("/change-role")
    public ResponseEntity<AuthResponse> changeUserRole(
            Authentication authentication,
            @Valid @RequestBody ChangeRoleRequest request
    ) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        AuthResponse.error("No autenticado")
                );
            }

            // Obtener usuario actual (quien realiza la solicitud)
            String email = authentication.getName();
            Optional<User> adminOpt = userService.getUserByEmail(email);

            if (adminOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        AuthResponse.error("Usuario no encontrado")
                );
            }

            User admin = adminOpt.get();

            // Verificar que el usuario es administrador
            if (admin.getRole() != Role.ADMIN) {
                log.warn("Intento no autorizado de cambiar rol por usuario: {}", email);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        AuthResponse.error("Solo administradores pueden cambiar roles")
                );
            }

            // Cambiar rol del usuario
            User updatedUser = userService.changeUserRole(request.getUserId(), request.getNewRole());
            UserDto userDto = UserDto.fromEntity(updatedUser);

            log.info("Rol cambiado para usuario ID {} a {} por admin {}", request.getUserId(), request.getNewRole(), email);

            return ResponseEntity.ok().body(
                    AuthResponse.success(
                            "Rol actualizado exitosamente",
                            null,
                            userDto
                    )
            );
        } catch (Exception e) {
            log.error("Error al cambiar rol: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    AuthResponse.error(e.getMessage())
            );
        }
    }
}