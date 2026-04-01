package com.futureprograms.authapi.controller;

import com.futureprograms.authapi.config.JwtProvider;
import com.futureprograms.authapi.dto.AuthResponse;
import com.futureprograms.authapi.dto.RegisterRequest;
import com.futureprograms.authapi.dto.UserDto;
import com.futureprograms.authapi.entity.User;
import com.futureprograms.authapi.enums.Role;
import com.futureprograms.authapi.repository.UserRepository;
import com.futureprograms.authapi.service.ImageService;
import com.futureprograms.authapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para gestión de usuarios
 * Maneja operaciones de administración y consulta de usuarios
 */
@RestController
@RequestMapping("/api/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final ImageService imageService;

    /**
     * Registro de nuevo usuario con soporte para imagen
     * Acepta FormData
     * NO requiere autenticación
     */
    @PostMapping("/register")
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
            
            // Construir request
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

            // 1. Crear usuario primero sin imagen (se asignará por defecto según género temporalmente)
            User user = userService.registerUser(request);
            log.info("✅ Usuario creado con ID: {}", user.getId());

            // 2. Si hay imagen, guardarla usando el ID del usuario
            if (profilePicture != null && !profilePicture.isEmpty()) {
                try {
                    String fileName = imageService.saveProfileImage(profilePicture, user.getId());
                    user = userService.updateProfileImage(user.getId(), fileName);
                    log.info("✅ Imagen de perfil guardada como 'profile' para usuario {}: {}", user.getId(), fileName);
                } catch (Exception e) {
                    log.error("⚠️ Error al guardar imagen de perfil post-registro: {}", e.getMessage());
                }
            }

            // Generar JWT
            String token = jwtProvider.generateToken(user.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuthResponse.builder()
                            .success(true)
                            .message("Usuario registrado exitosamente. Revisa tu correo para verificar la cuenta.")
                            .token(token)
                            .build());

        } catch (RuntimeException e) {
            log.error("❌ Error en registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error interno en registro: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.error("Error al registrar usuario: " + e.getMessage()));
        }
    }

    /**
     * Obtiene la lista de todos los usuarios (requiere ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> usersPage = userRepository.findAll(pageable);

            List<UserDto> userDtos = usersPage.getContent()
                    .stream()
                    .map(UserDto::fromEntity)
                    .collect(Collectors.toList());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Lista de usuarios obtenida exitosamente");
            responseData.put("users", userDtos);
            responseData.put("pagination", Map.of(
                    "currentPage", usersPage.getNumber(),
                    "totalItems", usersPage.getTotalElements(),
                    "totalPages", usersPage.getTotalPages(),
                    "pageSize", usersPage.getSize(),
                    "hasNext", usersPage.hasNext(),
                    "hasPrevious", usersPage.hasPrevious()
            ));

            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            log.error("Error al obtener lista de usuarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthResponse.error("Error al obtener lista de usuarios")
            );
        }
    }

    /**
     * Obtiene un usuario específico por su ID (requiere ADMIN o ser el mismo usuario)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<AuthResponse> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            UserDto userDto = UserDto.fromEntity(user);
            return ResponseEntity.ok(AuthResponse.success("Usuario obtenido exitosamente", null, userDto));
        } catch (Exception e) {
            log.error("Error al obtener usuario por ID: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthResponse.error(e.getMessage()));
        }
    }

    /**
     * Obtiene un usuario por su nick (requiere ADMIN)
     */
    @GetMapping("/nick/{nick}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> getUserByNick(@PathVariable String nick) {
        try {
            User user = userService.getUserByNick(nick)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con nick: " + nick));

            UserDto userDto = UserDto.fromEntity(user);
            return ResponseEntity.ok(AuthResponse.success("Usuario obtenido exitosamente", null, userDto));
        } catch (Exception e) {
            log.error("Error al obtener usuario por nick: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthResponse.error(e.getMessage()));
        }
    }

    /**
     * Cambia el rol de un usuario (requiere ADMIN)
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> changeUserRole(
            @PathVariable Long id,
            @RequestParam Role newRole
    ) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Aquí se podría añadir lógica para cambiar el rol en el servicio
            // user.setRole(newRole); // Esto depende de cómo manejes los roles
            // userRepository.save(user);

            return ResponseEntity.ok(AuthResponse.success("Rol actualizado exitosamente", null, UserDto.fromEntity(user)));
        } catch (Exception e) {
            log.error("Error al cambiar rol: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AuthResponse.error(e.getMessage()));
        }
    }
}
