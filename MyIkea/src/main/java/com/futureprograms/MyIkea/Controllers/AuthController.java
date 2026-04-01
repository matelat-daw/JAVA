package com.futureprograms.MyIkea.Controllers;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.futureprograms.MyIkea.Models.Auth.User;
import com.futureprograms.MyIkea.Services.auth.UserService;
import com.futureprograms.MyIkea.Services.FileUploadService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class AuthController {
    private final UserService userService;
    private final FileUploadService fileUploadService;

    public AuthController(UserService userService, FileUploadService fileUploadService) {
        this.userService = userService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                             BindingResult result,
                             @RequestParam("confirmPassword") String confirmPassword,
                             @RequestParam(value = "profilePictureFile", required = false) MultipartFile profilePictureFile,
                             Model model) {
        
        if (result.hasErrors()) {
            log.warn("Validation errors during registration: {}", result.getAllErrors());
            return "auth/register";
        }

        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("passwordError", "Passwords do not match");
            return "auth/register";
        }
        
        try {
            // Handle profile picture upload
            if (profilePictureFile != null && !profilePictureFile.isEmpty()) {
                try {
                    String fileName = fileUploadService.saveImage(profilePictureFile);
                    user.setProfilePicture(fileName);
                    log.info("Profile picture uploaded successfully: {}", fileName);
                } catch (Exception e) {
                    log.warn("Failed to upload profile picture, using default image: {}", e.getMessage());
                    user.setProfilePicture(getDefaultProfilePicture(user.getGender()));
                }
            } else {
                // Usar imagen por defecto según el género
                user.setProfilePicture(getDefaultProfilePicture(user.getGender()));
                log.info("Using default profile picture for gender: {}", user.getGender());
            }

            userService.register(user);
            log.info("User registered successfully: {}", user.getEmail());
            return "redirect:/registration-success";
        } catch (Exception e) {
            log.error("Error during user registration: ", e);
            model.addAttribute("error", "Error processing registration.");
            return "auth/register";
        }
    }

    /**
     * Get default profile picture based on gender
     * @param gender female, male, or other
     * @return filename of the default profile picture (without /images/ prefix)
     */
    private String getDefaultProfilePicture(String gender) {
        if (gender == null) {
            gender = "female";
        }
        
        return switch (gender.toLowerCase()) {
            case "male" -> "male.png";
            case "other" -> "other.png";
            default -> "female.png"; // Default to female
        };
    }

    @GetMapping("/registration-success")
    public String registrationSuccess() {
        return "auth/registration-success";
    }

    @GetMapping("/users")
    public String listUsers(Model model, Authentication authentication) {
        List<User> users = userService.getAllUsers()
                .stream()
                .filter(user -> !user.getUsername().equals(authentication.getName()))
                .collect(Collectors.toList());

        model.addAttribute("users", users);
        return "auth/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteById(id);
            log.info("Usuario con ID {} eliminado", id);
            return "redirect:/users";
        } catch (Exception e) {
            log.error("Error al eliminar usuario {}: ", id, e);
            return "redirect:/users?error=No se pudo eliminar el usuario";
        }
    }
}