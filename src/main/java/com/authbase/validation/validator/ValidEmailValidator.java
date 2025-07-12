package com.authbase.validation.validator;

import com.authbase.validation.annotation.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator implementation for the @ValidEmail annotation.
 * Provides enhanced email validation with configurable options.
 */
public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {

  private static final String EMAIL_PATTERN = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

  private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

  private boolean allowEmpty;
  private int maxLength;

  @Override
  public void initialize(ValidEmail constraintAnnotation) {
    this.allowEmpty = constraintAnnotation.allowEmpty();
    this.maxLength = constraintAnnotation.maxLength();
  }

  @Override
  public boolean isValid(String email, ConstraintValidatorContext context) {
    // Handle null/empty values
    if (email == null) {
      return allowEmpty;
    }

    String trimmedEmail = email.trim();
    if (trimmedEmail.isEmpty()) {
      return allowEmpty;
    }

    // Check length
    if (trimmedEmail.length() > maxLength) {
      return false;
    }

    // Check basic email format
    if (!pattern.matcher(trimmedEmail).matches()) {
      return false;
    }

    // Additional checks
    return isValidEmailStructure(trimmedEmail);
  }

  /**
   * Perform additional email structure validation.
   * 
   * @param email the email to validate
   * @return true if valid, false otherwise
   */
  private boolean isValidEmailStructure(String email) {
    // Check for @ symbol
    int atIndex = email.indexOf('@');
    if (atIndex == -1 || atIndex == 0 || atIndex == email.length() - 1) {
      return false;
    }

    // Split email into local and domain parts
    String localPart = email.substring(0, atIndex);
    String domainPart = email.substring(atIndex + 1);

    // Validate local part
    if (localPart.length() > 64 || localPart.isEmpty()) {
      return false;
    }

    // Validate domain part
    if (domainPart.length() > 253 || domainPart.isEmpty()) {
      return false;
    }

    // Check for valid domain structure
    if (!domainPart.contains(".") || domainPart.startsWith(".") || domainPart.endsWith(".")) {
      return false;
    }

    // Check for consecutive dots
    if (domainPart.contains("..") || localPart.contains("..")) {
      return false;
    }

    return true;
  }
}