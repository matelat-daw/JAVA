package com.futureprograms.MyIkea.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.futureprograms.MyIkea.Constants.AppConstants;
import com.futureprograms.MyIkea.Models.Auth.User;
import com.futureprograms.MyIkea.Utils.AuthUtils;

@ControllerAdvice
public class MainController {

    @ModelAttribute
    public void addAttributes(Model model, Authentication authentication) {
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
}
