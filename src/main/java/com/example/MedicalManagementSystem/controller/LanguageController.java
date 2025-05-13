package com.example.MedicalManagementSystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.Locale;

@Controller
public class LanguageController {

    @Autowired
    private LocaleResolver localeResolver;

    @GetMapping("/change-language")
    public RedirectView changeLanguage(
            @RequestParam("lang") String lang,
            @RequestParam(value = "redirect", required = false, defaultValue = "/") String redirect,
            HttpServletRequest request,
            HttpServletResponse response) {

        // Create a new locale based on the language parameter
        Locale newLocale = new Locale(lang);
        
        // Set the locale using the injected LocaleResolver
        localeResolver.setLocale(request, response, newLocale);
        
        // Set the locale in the current thread
        LocaleContextHolder.setLocale(newLocale);
        
        // Set a cookie with path=/
        Cookie cookie = new Cookie("app-locale", lang);
        cookie.setMaxAge(3600 * 24 * 30); // 30 days
        cookie.setPath("/");
        response.addCookie(cookie);
        
        // Set session attribute for additional reliability
        HttpSession session = request.getSession();
        session.setAttribute("org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE", newLocale);
        
        // Force a redirect to refresh with the new locale
        // Use RedirectView to ensure a proper redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(redirect);
        redirectView.setExposeModelAttributes(false);
        
        return redirectView;
    }
}

