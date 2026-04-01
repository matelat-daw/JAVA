package com.futureprograms.authapi.controller;

import com.futureprograms.authapi.config.JwtProvider;
import com.futureprograms.authapi.dto.AuthResponse;
import com.futureprograms.authapi.dto.RegisterRequest;
import com.futureprograms.authapi.entity.User;
import com.futureprograms.authapi.repository.UserRepository;
import com.futureprograms.authapi.service.ImageService;
import com.futureprograms.authapi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @deprecated Este controlador está DEPRECATED
 * El endpoint de registro ha sido movido a UserController en POST /api/users/register
 * 
 * Esta clase se mantiene solo para evitar errores de compilación.
 * Por favor usar: POST /api/users/register en su lugar
 */
@RestController
@RequestMapping("/register")
@Deprecated(forRemoval = true)
@Slf4j
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserRepository userRepository;

    /**
     * @deprecated Usar POST /api/users/register en su lugar
     */
    @Deprecated(forRemoval = true)
    @PostMapping
    public ResponseEntity<AuthResponse> register(
            @RequestParam String nick,
            @RequestParam String name,
            @RequestParam String surname1,
            @RequestParam(required = false) String surname2,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String gender,
            @RequestParam(required = false) String bday,
            @RequestParam(required = false) MultipartFile profilePicture
    ) {
        try {
            log.info("📝 Iniciando registro de usuario: {}", email);
            
            // Validar que el usuario no exista
            if (userRepository.findByEmail(email).isPresent()) {
                log.warn("❌ Email ya registrado: {}", email);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(AuthResponse.builder()
                                .success(false)
                                .message("El email ya está registrado")
                                .build());
            }

            if (userRepository.findByNick(nick).isPresent()) {
                log.warn("❌ Nick ya registrado: {}", nick);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(AuthResponse.builder()
                                .success(false)
                                .message("El nick ya está registrado")
                                .build());
            }

            // Construir usuario
            RegisterRequest request = RegisterRequest.builder()
                    .nick(nick)
                    .name(name)
                    .surname1(surname1)
                    .surname2(surname2)
                    .email(email)
                    .phone(phone)
                    .password(password)
                    .gender(gender)
                    .bday(bday != null && !bday.isEmpty() ? LocalDate.parse(bday, DateTimeFormatter.ISO_DATE) : null)
                    .build();

            // Procesar imagen si existe
            if (profilePicture != null && !profilePicture.isEmpty()) {
                try {
                    String fileName = imageService.saveProfileImage(profilePicture);
                    request.setProfileImg(fileName);
                    log.info("✅ Imagen de perfil guardada: {}", fileName);
                } catch (Exception e) {
                    log.warn("⚠️ Error al guardar imagen de perfil: {}", e.getMessage());
                    // Continuar sin imagen en caso de error
                }
            }

            // Crear usuario
            User user = userService.registerUser(request);
            log.info("✅ Usuario registrado exitosamente: {}", email);

            // Generar JWT
            String token = jwtProvider.generateToken(user.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuthResponse.builder()
                            .success(true)
                            .message("Usuario registrado exitosamente. Por favor verifica tu email.")
                            .token(token)
                            .build());

        } catch (IllegalArgumentException e) {
            log.error("❌ Validación fallida: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("❌ Error en registro: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .success(false)
                            .message("Error al registrar usuario: " + e.getMessage())
                            .build());
        }
    }
}
