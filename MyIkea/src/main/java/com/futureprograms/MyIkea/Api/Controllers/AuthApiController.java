package com.futureprograms.MyIkea.Api.Controllers;

import com.futureprograms.MyIkea.Api.Dto.AuthLoginRequest;
import com.futureprograms.MyIkea.Api.Dto.AuthRegisterRequest;
import com.futureprograms.MyIkea.Api.Dto.AuthResponse;
import com.futureprograms.MyIkea.Api.Mapper.ApiMapper;
import com.futureprograms.MyIkea.Models.Auth.User;
import com.futureprograms.MyIkea.Security.JwtService;
import com.futureprograms.MyIkea.Services.FileUploadService;
import com.futureprograms.MyIkea.Services.auth.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final FileUploadService fileUploadService;

    public AuthApiController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserDetailsService userDetailsService,
            UserService userService,
            FileUploadService fileUploadService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.fileUploadService = fileUploadService;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody AuthRegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "password_mismatch",
                    "message", "Las contrasenas no coinciden"
            ));
        }

        User user = buildRegisterUser(
                request.username(),
                request.email(),
                request.password(),
                request.gender(),
                null
        );

        try {
            userService.register(user);
            return ResponseEntity.status(201).body(Map.of("message", "Registro completado"));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "user_exists",
                    "message", "El usuario o email ya existe"
            ));
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerMultipart(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture
    ) {
        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "password_mismatch",
                    "message", "Las contrasenas no coinciden"
            ));
        }

        User user;
        try {
            user = buildRegisterUser(username, email, password, gender, profilePicture);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_register_data",
                    "message", ex.getMessage()
            ));
        }

        try {
            userService.register(user);
            return ResponseEntity.status(201).body(Map.of("message", "Registro completado"));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "user_exists",
                    "message", "El usuario o email ya existe"
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthLoginRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password())
            );

                jwtService.attachAuthCookies(response, authentication);

            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return ResponseEntity.ok(new AuthResponse("Login successful", ApiMapper.toUserProfileDto(user)));
            }

            return ResponseEntity.ok(Map.of("message", "Login successful"));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "invalid_credentials",
                    "message", "Usuario/email o contraseña incorrectos"
            ));
        } catch (DisabledException ex) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "user_disabled",
                    "message", "La cuenta está deshabilitada"
            ));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        var refreshToken = jwtService.resolveRefreshToken(request);
        if (refreshToken.isEmpty()) {
            jwtService.clearAuthCookies(response);
            return ResponseEntity.status(401).body(Map.of(
                    "error", "missing_refresh_token",
                    "message", "Refresh token ausente"
            ));
        }

        try {
            var username = jwtService.extractUsernameFromRefreshToken(refreshToken.get());
            if (username.isEmpty()) {
                jwtService.clearAuthCookies(response);
                return ResponseEntity.status(401).body(Map.of(
                        "error", "invalid_token",
                        "message", "Token inválido o expirado"
                ));
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username.get());

            if (!jwtService.isRefreshTokenValid(refreshToken.get(), userDetails.getUsername())) {
                jwtService.clearAuthCookies(response);
                return ResponseEntity.status(401).body(Map.of(
                        "error", "invalid_refresh_token",
                        "message", "Refresh token inválido"
                ));
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            jwtService.attachAuthCookies(response, authentication);

            if (userDetails instanceof User user) {
                return ResponseEntity.ok(new AuthResponse("Token refreshed", ApiMapper.toUserProfileDto(user)));
            }

            return ResponseEntity.ok(Map.of("message", "Token refreshed"));
        } catch (IllegalArgumentException ex) {
            jwtService.clearAuthCookies(response);
            return ResponseEntity.status(401).body(Map.of(
                    "error", "invalid_refresh_token",
                    "message", "Refresh token inválido o expirado"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        jwtService.clearAuthCookies(response);

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    private String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return "female";
        }
        String normalized = gender.trim().toLowerCase();
        return switch (normalized) {
            case "male", "female", "other" -> normalized;
            default -> "female";
        };
    }

    private String getDefaultProfilePicture(String gender) {
        return switch (gender) {
            case "male" -> "male.png";
            case "other" -> "other.png";
            default -> "female.png";
        };
    }

    private User buildRegisterUser(
            String username,
            String email,
            String password,
            String gender,
            MultipartFile profilePicture
    ) {
        User user = new User();
        user.setUsername(username == null ? "" : username.trim());
        user.setEmail(email == null ? "" : email.trim());
        user.setPassword(password);
        user.setGender(normalizeGender(gender));
        user.setProfilePicture(resolveProfilePicture(user.getGender(), profilePicture));
        return user;
    }

    private String resolveProfilePicture(String gender, MultipartFile profilePicture) {
        if (profilePicture == null || profilePicture.isEmpty()) {
            return getDefaultProfilePicture(gender);
        }

        try {
            return fileUploadService.saveImage(profilePicture);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error al subir imagen de perfil: " + ex.getMessage());
        }
    }
}
