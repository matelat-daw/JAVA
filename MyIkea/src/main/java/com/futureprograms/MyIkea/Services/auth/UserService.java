package com.futureprograms.MyIkea.Services.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.futureprograms.MyIkea.Models.Auth.Role;
import com.futureprograms.MyIkea.Models.Auth.User;
import com.futureprograms.MyIkea.Repositories.Auth.RoleRepository;
import com.futureprograms.MyIkea.Repositories.Auth.UserRepository;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("El rol 'USER' no existe en la base de datos"));
        user.setRoles(Collections.singletonList(userRole));
        userRepository.save(user);
    }

    public User findByUsername(String username) {
        if (username == null) throw new IllegalArgumentException("Username cannot be null");
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    public User findById(Integer id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    public User findByEmailIfExists(String email) {
        if (email == null) return null;
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public void update(User user) {
        if (user == null || user.getId() == null) throw new IllegalArgumentException("User and ID cannot be null");
        if (!userRepository.existsById(user.getId())) {
            throw new RuntimeException("Usuario no encontrado con ID: " + user.getId());
        }
        userRepository.save(user);
    }

    @Transactional
    public void updatePassword(Integer userId, String newPassword) {
        if (userId == null) throw new IllegalArgumentException("User ID cannot be null");
        User user = findById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void initializeRoles() {
        if (roleRepository.findByName("USER").isEmpty()) {
            Role roleUser = new Role();
            roleUser.setName("USER");
            roleRepository.save(roleUser);
        }

        if (roleRepository.findByName("MANAGER").isEmpty()) {
            Role roleManager = new Role();
            roleManager.setName("MANAGER");
            roleRepository.save(roleManager);
        }

        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role roleAdmin = new Role();
            roleAdmin.setName("ADMIN");
            roleRepository.save(roleAdmin);
        }
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        if (isAdmin) {
            throw new RuntimeException("No se puede eliminar un usuario con el rol ADMIN.");
        }

        userRepository.deleteById(id);
    }
}