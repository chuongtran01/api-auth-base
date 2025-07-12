package com.authbase.validation.validator;

import com.authbase.validation.annotation.StrongPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator implementation for the @StrongPassword annotation.
 * Validates password strength based on configurable requirements.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

  private int minLength;
  private int maxLength;
  private boolean requireUppercase;
  private boolean requireLowercase;
  private boolean requireDigit;
  private boolean requireSpecial;
  private String specialChars;

  @Override
  public void initialize(StrongPassword constraintAnnotation) {
    this.minLength = constraintAnnotation.minLength();
    this.maxLength = constraintAnnotation.maxLength();
    this.requireUppercase = constraintAnnotation.requireUppercase();
    this.requireLowercase = constraintAnnotation.requireLowercase();
    this.requireDigit = constraintAnnotation.requireDigit();
    this.requireSpecial = constraintAnnotation.requireSpecial();
    this.specialChars = constraintAnnotation.specialChars();
  }

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    if (password == null || password.trim().isEmpty()) {
      return false;
    }

    // Check length
    if (password.length() < minLength || password.length() > maxLength) {
      return false;
    }

    // Check for uppercase letters
    if (requireUppercase && !Pattern.compile("[A-Z]").matcher(password).find()) {
      return false;
    }

    // Check for lowercase letters
    if (requireLowercase && !Pattern.compile("[a-z]").matcher(password).find()) {
      return false;
    }

    // Check for digits
    if (requireDigit && !Pattern.compile("\\d").matcher(password).find()) {
      return false;
    }

    // Check for special characters
    if (requireSpecial) {
      String specialCharsRegex = Pattern.quote(specialChars);
      if (!Pattern.compile("[" + specialCharsRegex + "]").matcher(password).find()) {
        return false;
      }
    }

    return true;
  }
}