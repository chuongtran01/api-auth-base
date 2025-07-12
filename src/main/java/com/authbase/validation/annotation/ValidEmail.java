package com.authbase.validation.annotation;

import com.authbase.validation.validator.ValidEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for enhanced email validation.
 * Provides more comprehensive email validation than the standard @Email
 * annotation.
 */
@Documented
@Constraint(validatedBy = ValidEmailValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {

  /**
   * Default error message.
   * 
   * @return error message
   */
  String message() default "Please provide a valid email address";

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
   * Whether to allow empty/null values.
   * 
   * @return true if empty values are allowed
   */
  boolean allowEmpty() default false;

  /**
   * Maximum email length.
   * 
   * @return maximum length
   */
  int maxLength() default 254; // RFC 5321 limit
}