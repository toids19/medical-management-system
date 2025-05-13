package com.example.MedicalManagementSystem.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.logging.Logger;

@Controller
public class HomeController {

    //private static final Logger logger = Logger.getLogger(HomeController.class.getName());

    @GetMapping("/")
    public String home() {
        // Get the current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getAuthorities().stream().noneMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_USER"))) {
            //logger.info("User not authenticated or doesn't have required roles, redirecting to login");
            return "redirect:/login";
        }

       // logger.info("Home controller accessed by: " + authentication.getName());
        //logger.info("Is authenticated: " + authentication.isAuthenticated());
        //logger.info("Authorities: " + authentication.getAuthorities());

        // Check if user has ADMIN role
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            //logger.info("Redirecting to admin dashboard");
            return "redirect:/admin/dashboard";
        }

        // Default to user dashboard
        //logger.info("Redirecting to user dashboard");
        return "redirect:/user/dashboard";
    }
}

