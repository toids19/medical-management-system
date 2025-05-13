package com.example.MedicalManagementSystem.controller;

import com.example.MedicalManagementSystem.config.RecaptchaConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Autowired
    private RecaptchaConfig recaptchaConfig;

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        // If user is already authenticated, redirect to appropriate dashboard
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/user/dashboard";
            }
        }

        // Add reCAPTCHA site key to the model
        model.addAttribute("recaptchaSiteKey", recaptchaConfig.getRecaptchaSiteKey());
        return "login";
    }
}

