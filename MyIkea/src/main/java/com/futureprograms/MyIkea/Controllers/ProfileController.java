package com.futureprograms.MyIkea.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import com.futureprograms.MyIkea.Models.Auth.User;
import com.futureprograms.MyIkea.Services.auth.UserService;
import com.futureprograms.MyIkea.Services.FileUploadService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    private final UserService userService;
    private final FileUploadService fileUploadService;
    
    public ProfileController(UserService userService, FileUploadService fileUploadService) {
        this.userService = userService;
        this.fileUploadService = fileUploadService;
    }
    
    /**
     * Mostrar la página de perfil del usuario autenticado
     */
    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        return "profile/profile";
    }
    
    /**
     * Actualizar datos personales del usuario (nombre, apellido, teléfono, email)
     */
    @PostMapping("/update")
    public String updateProfile(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String phoneNumber,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            User user = userService.findByUsername(authentication.getName());
            
            // Validar que el email no esté siendo usado por otro usuario
            User existingEmailUser = userService.findByEmailIfExists(email);
            if (existingEmailUser != null && !existingEmailUser.getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "El email ya está registrado por otro usuario");
                return "redirect:/profile";
            }
            
            // Actualizar datos
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            
            userService.update(user);
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar perfil: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    /**
     * Cambiar la contraseña del usuario
     */
    @PostMapping("/update-password")
    public String updatePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            User user = userService.findByUsername(authentication.getName());
            
            // Validar que la contraseña actual sea correcta
            org.springframework.security.crypto.password.PasswordEncoder encoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            
            if (!encoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("passwordError", "La contraseña actual es incorrecta");
                return "redirect:/profile";
            }
            
            // Validar que las nuevas contraseñas coincidan
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("passwordError", "Las nuevas contraseñas no coinciden");
                return "redirect:/profile";
            }
            
            // Validar longitud mínima
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("passwordError", "La contraseña debe tener al menos 6 caracteres");
                return "redirect:/profile";
            }
            
            // Actualizar contraseña
            userService.updatePassword(user.getId(), newPassword);
            redirectAttributes.addFlashAttribute("passwordSuccess", "Contraseña actualizada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordError", "Error al cambiar contraseña: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    /**
     * Actualizar foto de perfil
     */
    @PostMapping("/update-picture")
    public String updateProfilePicture(
            @RequestParam("profilePicture") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            User user = userService.findByUsername(authentication.getName());
            
            // Validar que se proporcionó un archivo
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("pictureError", "Por favor selecciona una imagen");
                return "redirect:/profile";
            }
            
            // Eliminar imagen anterior si existe
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                try {
                    fileUploadService.deleteImage(user.getProfilePicture());
                } catch (Exception e) {
                    System.out.println("Error eliminando imagen anterior: " + e.getMessage());
                    // No bloqueamos si no se puede eliminar la anterior
                }
            }
            
            // Guardar nueva imagen
            String filename = fileUploadService.saveImage(file);
            user.setProfilePicture(filename);
            userService.update(user);
            
            redirectAttributes.addFlashAttribute("pictureSuccess", "Foto de perfil actualizada correctamente");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("pictureError", "Error al subir imagen: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("pictureError", "Error al actualizar foto: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    /**
     * Actualizar foto de perfil vía AJAX (REST endpoint)
     */
    @PostMapping(value = "/update-picture-ajax", produces = "application/json")
    public ResponseEntity<?> updateProfilePictureAjax(
            @RequestParam("profilePicture") MultipartFile file,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "No autenticado"));
        }
        
        try {
            User user = userService.findByUsername(authentication.getName());
            
            // Validar que se proporcionó un archivo
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Por favor selecciona una imagen"));
            }
            
            // Eliminar imagen anterior si existe
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                try {
                    fileUploadService.deleteImage(user.getProfilePicture());
                } catch (Exception e) {
                    System.out.println("Error eliminando imagen anterior: " + e.getMessage());
                }
            }
            
            // Guardar nueva imagen
            String filename = fileUploadService.saveImage(file);
            user.setProfilePicture(filename);
            userService.update(user);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Foto de perfil actualizada correctamente",
                    "filename", filename,
                    "imageUrl", "/images/" + filename
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Error al subir imagen: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "Error al actualizar foto: " + e.getMessage()));
        }
    }

    /**
     * Eliminar cuenta vía AJAX (REST endpoint)
     */
    @PostMapping(value = "/delete-ajax", consumes = "application/json", produces = "application/json")
    public org.springframework.http.ResponseEntity<?> deleteAccountAjax(
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> request,
            Authentication authentication,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return org.springframework.http.ResponseEntity.status(401)
                    .body(java.util.Map.of("success", false, "message", "No autenticado"));
        }
        
        try {
            // Validar que la contraseña se envió
            if (request == null || request.get("password") == null) {
                return org.springframework.http.ResponseEntity.badRequest()
                        .body(java.util.Map.of("success", false, "message", "Debes proporcionar tu contraseña"));
            }
            
            String password = request.get("password").trim();
            
            if (password.isEmpty()) {
                return org.springframework.http.ResponseEntity.badRequest()
                        .body(java.util.Map.of("success", false, "message", "La contraseña no puede estar vacía"));
            }
            
            // Obtener el usuario
            User user = userService.findByUsername(authentication.getName());
            if (user == null) {
                return org.springframework.http.ResponseEntity.status(404)
                        .body(java.util.Map.of("success", false, "message", "Usuario no encontrado"));
            }
            
            // Validar contraseña
            org.springframework.security.crypto.password.PasswordEncoder encoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            
            if (!encoder.matches(password, user.getPassword())) {
                return org.springframework.http.ResponseEntity.badRequest()
                        .body(java.util.Map.of("success", false, "message", "Contraseña incorrecta"));
            }
            
            // Eliminar foto de perfil si existe
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                try {
                    fileUploadService.deleteImage(user.getProfilePicture());
                } catch (Exception e) {
                    System.out.println("Error eliminando foto de perfil: " + e.getMessage());
                }
            }
            
            // Eliminar usuario
            userService.deleteById(user.getId());
            
            // Cerrar sesión después de eliminar cuenta
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(httpRequest, response, authentication);
            
            return org.springframework.http.ResponseEntity.ok()
                    .body(java.util.Map.of("success", true, "message", "Cuenta eliminada correctamente"));
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al eliminar cuenta: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500)
                    .body(java.util.Map.of("success", false, "message", "Error al eliminar cuenta: " + e.getMessage()));
        }
    }
}
