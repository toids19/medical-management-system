package com.example.MedicalManagementSystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RecaptchaConfig {

    @Value("${google.recaptcha.key.site:6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI}")
    private String recaptchaSiteKey;

    @Value("${google.recaptcha.key.secret:6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe}")
    private String recaptchaSecretKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getRecaptchaSiteKey() {
        return recaptchaSiteKey;
    }

    public String getRecaptchaSecretKey() {
        return recaptchaSecretKey;
    }
}