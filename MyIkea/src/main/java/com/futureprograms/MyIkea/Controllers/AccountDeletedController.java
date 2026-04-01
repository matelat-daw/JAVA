package com.futureprograms.MyIkea.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountDeletedController {
    
    /**
     * Mostrar página de confirmación de cuenta eliminada
     */
    @GetMapping("/account-deleted")
    public String accountDeleted() {
        return "auth/account-deleted";
    }
}
