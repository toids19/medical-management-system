package com.example.MedicalManagementSystem.controller;

import com.example.MedicalManagementSystem.config.RecaptchaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controller advice to add reCAPTCHA site key to all models
 * This allows us to use the reCAPTCHA in the login page without
 * having to modify the existing AuthController
 */
@ControllerAdvice
public class RecaptchaController {

    @Autowired
    private RecaptchaConfig recaptchaConfig;

    @ModelAttribute("recaptchaSiteKey")
    public String recaptchaSiteKey() {
        return recaptchaConfig.getRecaptchaSiteKey();
    }
}