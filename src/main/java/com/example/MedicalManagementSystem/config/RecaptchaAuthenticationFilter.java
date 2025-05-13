package com.example.MedicalManagementSystem.config;

import com.example.MedicalManagementSystem.service.RecaptchaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RecaptchaAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    //private static final Logger logger = LoggerFactory.getLogger(RecaptchaAuthenticationFilter.class);
    private final RecaptchaService recaptchaService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @Autowired
    public RecaptchaAuthenticationFilter(RecaptchaService recaptchaService, AuthenticationManager authenticationManager) {
        this.recaptchaService = recaptchaService;
        setAuthenticationManager(authenticationManager);

        // Set the URL that this filter should process
        setFilterProcessesUrl("/process-login");

        //logger.info("RecaptchaAuthenticationFilter initialized");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        //logger.info("Attempting authentication with reCAPTCHA validation");

        // Only validate reCAPTCHA for POST requests
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        // Get reCAPTCHA response
        String recaptchaResponse = request.getParameter("g-recaptcha-response");
        //logger.info("reCAPTCHA response: {}", recaptchaResponse != null && !recaptchaResponse.isEmpty() ? "present" : "missing");

        // Validate reCAPTCHA
        if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
            //logger.warn("reCAPTCHA validation failed: empty response");
            throw new RecaptchaAuthenticationException("reCAPTCHA validation failed: empty response");
        }

        //System.out.println("Validating reCAPTCHA response: " + (recaptchaResponse != null && !recaptchaResponse.isEmpty() ? "present" : "missing"));

        boolean isValidRecaptcha = recaptchaService.validateRecaptcha(recaptchaResponse);
        if (!isValidRecaptcha) {
            //logger.warn("reCAPTCHA validation failed");
            throw new RecaptchaAuthenticationException("reCAPTCHA validation failed");
        }

        //logger.info("reCAPTCHA validation successful, proceeding with authentication");

        // Get username and password
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        //logger.info("Authenticating user: {}", username);

        // Create authentication token
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);

        // Perform the authentication
        try {
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (AuthenticationException e) {
            //logger.error("Authentication failed for user {}: {}", username, e.getMessage());
            throw e;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
       // logger.info("Authentication successful for user: {}", authResult.getName());

        // Create a new security context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);

        // Store the security context in the session
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        //logger.debug("Set SecurityContextHolder to {}", authResult);

        // Determine where to redirect based on user role
        String targetUrl = determineTargetUrl(authResult);

        // Redirect to the target URL
        //logger.info("Redirecting to: {}", targetUrl);
        response.sendRedirect(targetUrl);
    }

    private String determineTargetUrl(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return "/admin/dashboard";
        } else {
            return "/user/dashboard";
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {

        //logger.warn("Authentication failed: {}", failed.getMessage());

        if (failed instanceof RecaptchaAuthenticationException) {
            // Redirect to login page with recaptcha error
            //logger.info("Redirecting to login page with reCAPTCHA error");
            response.sendRedirect("/login?error=recaptcha");
        } else {
            // Use the default behavior for other authentication failures
            //logger.info("Redirecting to login page with authentication error");
            response.sendRedirect("/login?error=true");
        }
    }

    public static class RecaptchaAuthenticationException extends AuthenticationException {
        public RecaptchaAuthenticationException(String msg) {
            super(msg);
        }
    }
}

