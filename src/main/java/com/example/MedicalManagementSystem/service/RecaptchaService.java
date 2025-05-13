package com.example.MedicalManagementSystem.service;

import com.example.MedicalManagementSystem.config.RecaptchaConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class RecaptchaService {

    private static final String GOOGLE_RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    //private static final Logger logger = Logger.getLogger(RecaptchaService.class.getName());

    @Autowired
    private RecaptchaConfig recaptchaConfig;

    @Autowired
    private RestTemplate restTemplate;

    public boolean validateRecaptcha(String recaptchaResponse) {
        // Skip validation if recaptcha is not configured or response is empty
        if (recaptchaConfig.getRecaptchaSecretKey() == null ||
                recaptchaConfig.getRecaptchaSecretKey().isEmpty() ||
                recaptchaResponse == null ||
                recaptchaResponse.isEmpty()) {
            //logger.warning("reCAPTCHA validation skipped: missing configuration or empty response");
            return false;
        }

        try {
            //System.out.println("Sending reCAPTCHA validation request to Google");

            MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
            requestMap.add("secret", recaptchaConfig.getRecaptchaSecretKey());
            requestMap.add("response", recaptchaResponse);

            RecaptchaResponse response = restTemplate.postForObject(
                    GOOGLE_RECAPTCHA_VERIFY_URL,
                    requestMap,
                    RecaptchaResponse.class);

            if (response == null) {
                //logger.warning("reCAPTCHA validation failed: null response from Google");
                return false;
            }

            // For reCAPTCHA v2, we only check success
            //System.out.println("reCAPTCHA response success: " + response.isSuccess());

            if (response.isSuccess()) {
                //logger.info("reCAPTCHA validation successful");
                return true;
            } else {
                //logger.warning("reCAPTCHA validation failed: " +
                        //(response.getErrorCodes() != null ? String.join(", ", response.getErrorCodes()) : "unknown error"));
                return false;
            }
        } catch (RestClientException e) {
            //logger.log(Level.SEVERE, "Error during reCAPTCHA validation", e);
            return false;
        }
    }

    static class RecaptchaResponse {
        private boolean success;
        private Float score;
        private String action;
        @JsonProperty("challenge_ts")
        private String challengeTs;
        private String hostname;
        @JsonProperty("error-codes")
        private String[] errorCodes;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Float getScore() {
            return score;
        }

        public void setScore(Float score) {
            this.score = score;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getChallengeTs() {
            return challengeTs;
        }

        public void setChallengeTs(String challengeTs) {
            this.challengeTs = challengeTs;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String[] getErrorCodes() {
            return errorCodes;
        }

        public void setErrorCodes(String[] errorCodes) {
            this.errorCodes = errorCodes;
        }
    }
}

