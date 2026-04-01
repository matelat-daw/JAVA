package com.futureprograms.authapi.service;

import com.futureprograms.authapi.dto.RegisterRequest;
import com.futureprograms.authapi.entity.RoleEntity;
import com.futureprograms.authapi.entity.User;
import com.futureprograms.authapi.enums.Gender;
import com.futureprograms.authapi.enums.Role;
import com.futureprograms.authapi.repository.RoleRepository;
import com.futureprograms.authapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final ImageService imageService;

    /**
     * Registra un nuevo usuario
     */
    @Transactional
    public User registerUser(RegisterRequest request) {
        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Validar que el nick no exista
        if (userRepository.existsByNick(request.getNick())) {
            throw new RuntimeException("El nick ya está en uso");
        }

        // Convertir gender string a enum
        Gender gender = Gender.fromEnglishName(request.getGender());

        RoleEntity userRole = roleRepository.findByName(Role.USER.name())
            .orElseThrow(() -> new RuntimeException("El rol USER no existe en la tabla roles"));

        // Asignar imagen por defecto si no se proporciona
        String profileImg = request.getProfileImg();
        if (profileImg == null || profileImg.trim().isEmpty()) {
            profileImg = gender.getDefaultImagePath();
            log.info("Asignando imagen por defecto para gender: {}", gender.getDisplayName());
        }

        // Crear nuevo usuario
        User user = User.builder()
                .nick(request.getNick())
                .name(request.getName())
                .surname1(request.getSurname1())
                .surname2(request.getSurname2())
                .phone(request.getPhone())
                .gender(gender)
                .bday(request.getBday())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .profileImg(profileImg)
                .roles(new HashSet<>(Set.of(userRole)))
                .active(false)
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .build();

        user = userRepository.save(user);

        log.info("Usuario registrado: {}", user.getEmail());

        // Enviar email de verificación
        try {
            emailService.sendVerificationEmail(
                    user.getEmail(),
                    user.getName(),
                    user.getVerificationToken()
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar email de verificacion a {}: {}", user.getEmail(), e.getMessage());
            // No interrumpir el registro si falla el proveedor de correo.
        }

        return user;
    }

    /**
     * Verifica el email del usuario
     */
    @Transactional
    public User verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Token de verificación inválido");
        }

        User user = userOpt.get();

        // Verificar que el token no haya expirado
        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token de verificación ha expirado");
        }

        // Marcar como verificado
        user.setEmailVerified(true);
        user.setActive(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        user = userRepository.save(user);

        log.info("Email verificado para usuario: {}", user.getEmail());

        // Enviar email de bienvenida
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());

        return user;
    }

    /**
     * Obtiene un usuario por email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Obtiene un usuario por nick
     */
    public Optional<User> getUserByNick(String nick) {
        return userRepository.findByNick(nick);
    }

    /**
     * Obtiene un usuario por ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Valida las credenciales del usuario
     */
    public boolean validateCredentials(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        if (!user.getActive() || !user.getEmailVerified()) {
            throw new RuntimeException("Usuario no verificado o inactivo");
        }

        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * Actualiza el perfil del usuario
     */
    public User updateUserProfile(Long id, String name, String surname1, String surname2, String phone) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        User user = userOpt.get();
        user.setName(name);
        user.setSurname1(surname1);
        user.setSurname2(surname2);
        user.setPhone(phone);

        return userRepository.save(user);
    }

    /**
     * Actualiza la imagen de perfil de un usuario
     */
    @Transactional
    public User updateProfileImage(Long userId, String imagePath) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Eliminar imagen anterior si no es por defecto
        if (user.getProfileImg() != null && !imageService.isProtectedImage(user.getProfileImg())) {
            try {
                imageService.deleteImage(user.getProfileImg());
            } catch (Exception e) {
                log.warn("No se pudo eliminar imagen anterior de usuario {}: {}", userId, e.getMessage());
            }
        }

        user.setProfileImg(imagePath);
        return userRepository.save(user);
    }

    /**
     * Cambia la contraseña del usuario
     */
    public User changePassword(Long id, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        User user = userOpt.get();

        // Validar contraseña anterior
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        // Validar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("La nueva contraseña no puede ser igual a la actual");
        }

        // Cambiar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        user = userRepository.save(user);

        log.info("Contraseña cambiada para usuario: {}", user.getEmail());
        return user;
    }

    /**
     * Elimina la cuenta del usuario (requiere confirmación con contraseña)
     */
    public void deleteUserAccount(Long id, String password) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        User user = userOpt.get();

        // Validar contraseña
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta - no se puede eliminar la cuenta");
        }

        // Elim Inar usuario
        userRepository.delete(user);
        log.info("Cuenta de usuario eliminada: {}", user.getEmail());
    }

    /**
     * Valida si una imagen es protegida (por defecto)
     */
    public boolean isProtectedImage(String imagePath) {
        return Gender.isDefaultImagePath(imagePath);
    }

    /**
     * Obtiene la imagen por defecto para un género
     */
    public String getDefaultImageForGender(String genderDisplayName) {
        Gender gender = Gender.fromDisplayName(genderDisplayName);
        return gender.getDefaultImagePath();
    }

    /**
     * Cambia el rol de un usuario (solo para administradores)
     */
    public User changeUserRole(Long userId, String newRoleName) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        if (!Role.isValidRole(newRoleName)) {
            throw new RuntimeException("Rol inválido: " + newRoleName);
        }

        User user = userOpt.get();
        Role newRole = Role.fromDisplayName(newRoleName);
        RoleEntity roleEntity = roleRepository.findByName(newRole.name())
            .orElseThrow(() -> new RuntimeException("El rol " + newRole.name() + " no existe en la tabla roles"));
        user.setRoles(new HashSet<>(Set.of(roleEntity)));
        user = userRepository.save(user);

        log.info("Rol del usuario {} cambiado a {}", user.getEmail(), newRole.getDisplayName());
        return user;
    }

    /**
     * Obtiene el rol del usuario
     */
    public Role getUserRole(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return userOpt.get().getRole();
    }

    /**
     * Verifica si un usuario tiene un rol específico
     */
    public boolean userHasRole(Long userId, Role role) {
        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.isPresent() && userOpt.get().getRole() == role;
    }

    /**
     * Verifica si un usuario es administrador
     */
    public boolean isAdmin(Long userId) {
        return userHasRole(userId, Role.ADMIN);
    }

    /**
     * Verifica si un usuario tiene rol premium o superior
     */
    public boolean isPremiumOrAdmin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        Role role = userOpt.get().getRole();
        return role == Role.PREMIUM || role == Role.ADMIN;
    }
}
