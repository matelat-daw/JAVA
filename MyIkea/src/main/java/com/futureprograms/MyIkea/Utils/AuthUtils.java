package com.futureprograms.MyIkea.Utils;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Utilidades para verificación de autenticación y autorización
 */
public class AuthUtils {
    
    /**
     * Verifica si un usuario está autenticado
     */
    public static boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
    
    /**
     * Verifica si un usuario tiene un rol específico
     */
    public static boolean hasRole(Authentication authentication, String role) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }
    
    /**
     * Verifica si un usuario tiene alguno de los roles especificados
     */
    public static boolean hasAnyRole(Authentication authentication, String... roles) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        for (String role : roles) {
            if (hasRole(authentication, role)) {
                return true;
            }
        }
        return false;
    }
    
    private AuthUtils() {
        // Clase de utilidades, no se puede instanciar
    }
}
