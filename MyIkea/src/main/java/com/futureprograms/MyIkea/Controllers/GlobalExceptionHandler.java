package com.futureprograms.MyIkea.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.NoHandlerFoundException;
import jakarta.persistence.EntityNotFoundException;
import com.futureprograms.MyIkea.Constants.AppConstants;
import com.futureprograms.MyIkea.Models.Auth.User;
import com.futureprograms.MyIkea.Utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ✅ INYECTAR VARIABLES DE SEGURIDAD EN TODAS LAS EXCEPCIONES
    @ModelAttribute
    public void addSecurityAttributes(Model model, Authentication authentication) {
        boolean isLogged = AuthUtils.isAuthenticated(authentication);
        boolean isAdmin = AuthUtils.hasRole(authentication, AppConstants.ROLE_ADMIN);
        boolean isManager = AuthUtils.hasRole(authentication, AppConstants.ROLE_MANAGER);
        String email = null;

        if (isLogged && authentication.getPrincipal() instanceof User logUser) {
            email = logUser.getEmail();
        }

        model.addAttribute(AppConstants.ATTR_LOGGED, isLogged);
        model.addAttribute(AppConstants.ATTR_ADMIN, isAdmin);
        model.addAttribute(AppConstants.ATTR_MANAGER, isManager);
        model.addAttribute(AppConstants.ATTR_EMAIL, email);
    }

    // ✅ IGNORAR SILENCIOSAMENTE ERRORES DE RECURSOS NO ENCONTRADOS (imagenes, archivos estaticos)
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public String handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        // Solo registrar a nivel DEBUG, no como ERROR
        log.debug("Recurso estatico no encontrado: {}", ex.getResourcePath());
        return "forward:/images/default.jpg";
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException ex, Model model) {
        log.error("Entidad no encontrada: {}", ex.getMessage());
        model.addAttribute("error", "El recurso solicitado no existe.");
        return "error";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNoHandlerFoundException(NoHandlerFoundException ex, Model model) {
        log.error("Página no encontrada: {}", ex.getRequestURL());
        model.addAttribute("error", "La página que buscas no existe.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Error inesperado: ", ex);
        model.addAttribute("error", "Ha ocurrido un error inesperado. Por favor, inténtalo de nuevo más tarde.");
        return "error";
    }
}
