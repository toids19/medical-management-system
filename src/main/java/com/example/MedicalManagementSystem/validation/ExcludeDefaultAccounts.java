package com.example.MedicalManagementSystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation that excludes default accounts from validation.
 * This allows the default 'admin' and 'user' accounts to bypass certain validation rules.
 */
@Documented
@Constraint(validatedBy = ExcludeDefaultAccountsValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcludeDefaultAccounts {
    String message() default "Validation failed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
