package com.authbase.validation.annotation;

import com.authbase.validation.validator.StrongPasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for strong password requirements.
 * Validates that a password meets security requirements.
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

  /**
   * Default error message.
   * 
   * @return error message
   */
  String message() default "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character";

  /**
   * Validation groups.
   * 
   * @return validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Payload.
   * 
   * @return payload
   */
  Class<? extends Payload>[] payload() default {};

  /**
   * Minimum password length.
   * 
   * @return minimum length
   */
  int minLength() default 8;

  /**
   * Maximum password length.
   * 
   * @return maximum length
   */
  int maxLength() default 128;

  /**
   * Whether to require uppercase letters.
   * 
   * @return true if required
   */
  boolean requireUppercase() default true;

  /**
   * Whether to require lowercase letters.
   * 
   * @return true if required
   */
  boolean requireLowercase() default true;

  /**
   * Whether to require digits.
   * 
   * @return true if required
   */
  boolean requireDigit() default true;

  /**
   * Whether to require special characters.
   * 
   * @return true if required
   */
  boolean requireSpecial() default true;

  /**
   * Special characters pattern.
   * 
   * @return regex pattern for special characters
   */
  String specialChars() default "!@#$%^&*()_+-=[]{}|;:,.<>?";
}