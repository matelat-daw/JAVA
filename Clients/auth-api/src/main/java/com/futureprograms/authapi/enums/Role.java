package com.futureprograms.authapi.enums;

/**
 * Enum para los roles de usuario en el sistema
 */
public enum Role {
    USER("Usuario estándar"),
    PREMIUM("Usuario premium con funcionalidades avanzadas"),
    ADMIN("Administrador del sistema");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayName() {
        return this.name();
    }

    /**
     * Convierte una cadena de texto al enum Role correspondiente
     * @param displayName Nombre del rol en mayúsculas
     * @return Role correspondiente
     * @throws IllegalArgumentException si el rol no existe
     */
    public static Role fromDisplayName(String displayName) {
        try {
            return Role.valueOf(displayName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol inválido: " + displayName);
        }
    }

    /**
     * Verifica si un rol es válido
     */
    public static boolean isValidRole(String roleName) {
        try {
            Role.valueOf(roleName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
