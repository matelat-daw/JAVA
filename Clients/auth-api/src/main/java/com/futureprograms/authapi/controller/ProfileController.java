package com.futureprograms.authapi.controller;

import com.futureprograms.authapi.dto.AuthResponse;
import com.futureprograms.authapi.dto.ProfileDeleteRequest;
import com.futureprograms.authapi.dto.UpdatePasswordRequest;
import com.futureprograms.authapi.dto.UpdateProfileImageRequest;
import com.futureprograms.authapi.dto.UpdateProfileRequest;
import com.futureprograms.authapi.dto.UserDto;
import com.futureprograms.authapi.entity.User;
import com.futureprograms.authapi.service.ImageService;
import com.futureprograms.authapi.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para la gestión del perfil del usuario
 * Maneja todas las operaciones relacionadas con el perfil: actualización, cambio de contraseña, etc.
 * Compatible tanto con auth-api como con patrones de MyIkea-Frontend
 */
@RestController
@RequestMapping("/api/profile")
@Slf4j
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    /**
     * Obtiene el perfil del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<AuthResponse> getProfile(Authentication authentication) {
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

            UserDto userDto = UserDto.fromEntity(userOpt.get());

            log.info("Perfil obtenido para usuario: {}", email);

            return ResponseEntity.ok().body(
                    AuthResponse.success("Perfil obtenido exitosamente", null, userDto)
            );
        } catch (Exception e) {
            log.error("Error al obtener perfil: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Actualiza los datos del perfil del usuario
     */
    @PutMapping
    public ResponseEntity<AuthResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
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
            Long userId = user.getId();

            // Actualizar perfil
            User updatedUser = userService.updateUserProfile(
                    userId,
                    request.getName(),
                    request.getSurname1(),
                    request.getSurname2(),
                    request.getPhone()
            );

            UserDto userDto = UserDto.fromEntity(updatedUser);

            log.info("Perfil actualizado para usuario: {}", email);

            return ResponseEntity.ok().body(
                    AuthResponse.success("Perfil actualizado exitosamente", null, userDto)
            );
        } catch (Exception e) {
            log.error("Error al actualizar perfil: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    AuthResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Sube una nueva foto de perfil (MultipartFile)
     * Compatible con MyIkea-Frontend
     */
    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateProfilePicture(
            Authentication authentication,
            @RequestParam("profilePicture") MultipartFile file
    ) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "No autenticado")
                );
            }

            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of("success", false, "message", "Por favor selecciona una imagen")
                );
            }

            String email = authentication.getName();
            Optional<User> userOpt = userService.getUserByEmail(email);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("success", false, "message", "Usuario no encontrado")
                );
            }

            User user = userOpt.get();
            Long userId = user.getId();

            // Es una operación de actualización si es multipart
            String fileName = imageService.saveProfileImage(file, userId);
            user = userService.updateProfileImage(userId, fileName);

            UserDto userDto = UserDto.fromEntity(user);

            log.info("Foto de perfil actualizada para usuario: {}", userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Foto de perfil actualizada correctamente");
            response.put("filename", fileName);
            response.put("imageUrl", "/images/" + fileName);
            response.put("profile", userDto);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al actualizar foto de perfil: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Error al subir imagen: " + e.getMessage())
            );
        }
    }

    /**
     * Actualiza la foto de perfil con URL (legacy)
     */
    @PutMapping("/image")
    public ResponseEntity<AuthResponse> updateProfileImage(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileImageRequest request
    ) {
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
            Long userId = user.getId();

            // Validar URL de imagen
            if (request.getProfileImageUrl() == null || request.getProfileImageUrl().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        AuthResponse.error("La URL de la imagen es requerida")
                );
            }

            // Actualizar imagen de perfil
            User updatedUser = userService.updateProfileImage(userId, request.getProfileImageUrl());

            UserDto userDto = UserDto.fromEntity(updatedUser);

            log.info("Foto de perfil actualizada para usuario: {}", email);

            return ResponseEntity.ok().body(
                    AuthResponse.success("Foto de perfil actualizada exitosamente", null, userDto)
            );
        } catch (Exception e) {
            log.error("Error al actualizar foto de perfil: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    AuthResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Cambia la contraseña del usuario
     */
    @PutMapping("/password")
    public ResponseEntity<AuthResponse> updatePassword(
            Authentication authentication,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        AuthResponse.error("No autenticado")
                );
            }

            // Validar que las contraseñas coincidan
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        AuthResponse.error("Las nuevas contraseñas no coinciden")
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
            Long userId = user.getId();

            // Cambiar contraseña
            User updatedUser = userService.changePassword(
                    userId,
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );

            UserDto userDto = UserDto.fromEntity(updatedUser);

            log.info("Contraseña actualizada para usuario: {}", email);

            return ResponseEntity.ok().body(
                    AuthResponse.success("Contraseña actualizada exitosamente", null, userDto)
            );
        } catch (RuntimeException e) {
            log.warn("Error de contraseña para usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    AuthResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            log.error("Error al cambiar contraseña: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Elimina la cuenta del usuario vía JSON (POST)
     * Compatible con MyIkea-Frontend
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteAccountPost(
            Authentication authentication,
            @Valid @RequestBody ProfileDeleteRequest request
    ) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "No autenticado")
                );
            }

            String email = authentication.getName();
            Optional<User> userOpt = userService.getUserByEmail(email);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("success", false, "message", "Usuario no encontrado")
                );
            }

            User user = userOpt.get();
            Long userId = user.getId();

            // Intentar eliminar imagen de perfil si no es protegida
            if (user.getProfileImg() != null && !user.getProfileImg().isEmpty()) {
                try {
                    if (!imageService.isProtectedImage(user.getProfileImg())) {
                        imageService.deleteImage(user.getProfileImg());
                        log.info("Imagen de perfil eliminada: {}", user.getProfileImg());
                    } else {
                        log.info("No se elimina imagen protegida para usuario: {}", email);
                    }
                } catch (Exception e) {
                    log.warn("No se pudo eliminar imagen del usuario: {}", e.getMessage());
                }
            }

            // Eliminar cuenta
            userService.deleteUserAccount(userId, request.getPassword());

            log.info("Cuenta de usuario eliminada: {}", email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cuenta eliminada correctamente");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Error al eliminar cuenta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("success", false, "message", e.getMessage())
            );
        } catch (Exception e) {
            log.error("Error al eliminar cuenta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Error al eliminar cuenta: " + e.getMessage())
            );
        }
    }

    /**
     * Valida la contraseña del usuario autenticado
     * Usado para confirmar acciones sensibles como eliminar la cuenta
     */
    @PostMapping("/validate-password")
    public ResponseEntity<Map<String, Object>> validatePassword(
            Authentication authentication,
            @Valid @RequestBody Map<String, String> request
    ) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "No autenticado")
                );
            }

            String password = request.get("password");
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of("success", false, "message", "La contraseña es requerida")
                );
            }

            String email = authentication.getName();
            Optional<User> userOpt = userService.getUserByEmail(email);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("success", false, "message", "Usuario no encontrado")
                );
            }

            // Validar contraseña
            if (userService.validateCredentials(email, password)) {
                log.info("Contraseña validada correctamente para usuario: {}", email);
                return ResponseEntity.ok().body(
                        Map.of("success", true, "message", "Contraseña correcta")
                );
            } else {
                log.warn("Intento de validación con contraseña incorrecta para usuario: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "Contraseña incorrecta")
                );
            }
        } catch (Exception e) {
            log.error("Error al validar contraseña: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Error al validar contraseña: " + e.getMessage())
            );
        }
    }

    /**
     * Elimina la cuenta del usuario vía DELETE (REST style)
     */
    @DeleteMapping
    public ResponseEntity<AuthResponse> deleteProfile(
            Authentication authentication,
            @RequestBody Map<String, String> requestBody
    ) {
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

            // Obtener contraseña del body
            String password = requestBody.get("password");
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        AuthResponse.error("La contraseña es requerida para eliminar la cuenta")
                );
            }

            User user = userOpt.get();
            Long userId = user.getId();

            // Intentar eliminar imagen de perfil si no es protegida
            if (user.getProfileImg() != null && !user.getProfileImg().isEmpty()) {
                try {
                    if (!imageService.isProtectedImage(user.getProfileImg())) {
                        imageService.deleteImage(user.getProfileImg());
                        log.info("Imagen de perfil eliminada: {}", user.getProfileImg());
                    } else {
                        log.info("No se elimina imagen protegida para usuario: {}", email);
                    }
                } catch (Exception e) {
                    log.warn("No se pudo eliminar imagen del usuario: {}", e.getMessage());
                }
            }

            // Eliminar cuenta
            userService.deleteUserAccount(userId, password);

            log.info("Cuenta de usuario eliminada: {}", email);

            return ResponseEntity.ok().body(
                    AuthResponse.success("Cuenta eliminada exitosamente", null, null)
            );
        } catch (RuntimeException e) {
            log.warn("Error al eliminar cuenta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    AuthResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            log.error("Error al eliminar cuenta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error(e.getMessage())
            );
        }
    }
}
