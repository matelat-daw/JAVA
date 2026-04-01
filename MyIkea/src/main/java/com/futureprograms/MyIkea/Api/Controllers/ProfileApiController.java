package com.futureprograms.MyIkea.Api.Controllers;

import com.futureprograms.MyIkea.Api.Dto.ProfileDeleteRequest;
import com.futureprograms.MyIkea.Api.Dto.ProfilePasswordUpdateRequest;
import com.futureprograms.MyIkea.Api.Dto.ProfileUpdateRequest;
import com.futureprograms.MyIkea.Api.Dto.UserProfileDto;
import com.futureprograms.MyIkea.Api.Mapper.ApiMapper;
import com.futureprograms.MyIkea.Models.Auth.User;
import com.futureprograms.MyIkea.Security.JwtService;
import com.futureprograms.MyIkea.Services.FileUploadService;
import com.futureprograms.MyIkea.Services.auth.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileApiController {

    private final UserService userService;
    private final FileUploadService fileUploadService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public ProfileApiController(
            UserService userService,
            FileUploadService fileUploadService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.fileUploadService = fileUploadService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(ApiMapper.toUserProfileDto(user));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User authUser,
            @RequestBody ProfileUpdateRequest request
    ) {
        if (authUser == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userService.findById(authUser.getId());

        if (request.email() != null && !request.email().isBlank()) {
            User existing = userService.findByEmailIfExists(request.email().trim());
            if (existing != null && !existing.getId().equals(user.getId())) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "email_exists",
                        "message", "El email ya esta registrado"
                ));
            }
            user.setEmail(request.email().trim());
        }

        if (request.firstName() != null) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName().trim());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber().trim());
        }
        if (request.gender() != null && !request.gender().isBlank()) {
            String gender = request.gender().trim().toLowerCase();
            if (gender.equals("male") || gender.equals("female") || gender.equals("other")) {
                user.setGender(gender);
            }
        }

        userService.update(user);
        return ResponseEntity.ok(ApiMapper.toUserProfileDto(user));
    }

    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePicture(
            @AuthenticationPrincipal User authUser,
            @RequestParam("profilePicture") MultipartFile file
    ) {
        if (authUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "empty_picture",
                    "message", "Por favor selecciona una imagen"
            ));
        }

        User user = userService.findById(authUser.getId());

        try {
            if (user.getProfilePicture() != null && !user.getProfilePicture().isBlank()) {
                fileUploadService.deleteImage(user.getProfilePicture());
            }

            String filename = fileUploadService.saveImage(file);
            user.setProfilePicture(filename);
            userService.update(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Foto de perfil actualizada correctamente",
                    "filename", filename,
                    "imageUrl", "/images/" + filename,
                    "profile", ApiMapper.toUserProfileDto(user)
            ));
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_picture",
                    "message", "Error al subir imagen: " + ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "picture_update_failed",
                    "message", "Error al actualizar foto: " + ex.getMessage()
            ));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(
            @AuthenticationPrincipal User authUser,
            @RequestBody ProfilePasswordUpdateRequest request
    ) {
        if (authUser == null) {
            return ResponseEntity.status(401).build();
        }

        String currentPassword = request.currentPassword() == null ? "" : request.currentPassword().trim();
        String newPassword = request.newPassword() == null ? "" : request.newPassword().trim();
        String confirmPassword = request.confirmPassword() == null ? "" : request.confirmPassword().trim();

        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "missing_password_fields",
                    "message", "Todos los campos de contrasena son obligatorios"
            ));
        }

        User user = userService.findById(authUser.getId());

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_current_password",
                    "message", "La contrasena actual es incorrecta"
            ));
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "password_mismatch",
                    "message", "Las nuevas contrasenas no coinciden"
            ));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "password_too_short",
                    "message", "La contrasena debe tener al menos 6 caracteres"
            ));
        }

        userService.updatePassword(user.getId(), newPassword);
        return ResponseEntity.ok(Map.of("message", "Contrasena actualizada correctamente"));
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteAccount(
            @AuthenticationPrincipal User authUser,
            @RequestBody ProfileDeleteRequest request,
            HttpServletResponse response
    ) {
        if (authUser == null) {
            return ResponseEntity.status(401).build();
        }

        String password = request == null || request.password() == null ? "" : request.password().trim();
        if (password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "missing_password",
                    "message", "Debes proporcionar tu contrasena"
            ));
        }

        User user = userService.findById(authUser.getId());

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_password",
                    "message", "Contrasena incorrecta"
            ));
        }

        if (user.getProfilePicture() != null && !user.getProfilePicture().isBlank()) {
            fileUploadService.deleteImage(user.getProfilePicture());
        }

        try {
            userService.deleteById(user.getId());
            jwtService.clearAuthCookies(response);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cuenta eliminada correctamente"
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "delete_not_allowed",
                    "message", ex.getMessage()
            ));
        }
    }
}
