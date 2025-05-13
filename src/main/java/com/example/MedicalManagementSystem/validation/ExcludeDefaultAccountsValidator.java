package com.example.MedicalManagementSystem.validation;

import com.example.MedicalManagementSystem.model.User;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

/**
 * Validator implementation for the ExcludeDefaultAccounts annotation.
 * This validator checks if the user is a default account and applies different validation rules.
 */
public class ExcludeDefaultAccountsValidator implements ConstraintValidator<ExcludeDefaultAccounts, User> {

    private static final List<String> DEFAULT_ACCOUNTS = Arrays.asList("admin", "user");

    @Override
    public void initialize(ExcludeDefaultAccounts constraintAnnotation) {
        // Initialization if needed
    }

    @Override
    public boolean isValid(User user, ConstraintValidatorContext context) {
        if (user == null) {
            return true; // Let other validators handle null validation
        }

        // If it's a default account, skip validation
        if (DEFAULT_ACCOUNTS.contains(user.getUsername())) {
            return true;
        }

        // For non-default accounts, apply full validation
        // Disable the default error message
        context.disableDefaultConstraintViolation();

        boolean isValid = true;

        // Validate username (for non-default accounts)
        if (user.getUsername() != null && !user.getUsername().matches("^[a-zA-Z0-9_.-]{3,50}$")) {
            context.buildConstraintViolationWithTemplate(
                            "Username must be 3-50 characters and can contain letters, numbers, dots, underscores, and hyphens")
                    .addPropertyNode("username")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate password (for non-default accounts)
        if (user.getPassword() != null && !user.getPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            context.buildConstraintViolationWithTemplate(
                            "Password must be at least 8 characters and include at least one digit, one lowercase letter, one uppercase letter, and one special character")
                    .addPropertyNode("password")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
