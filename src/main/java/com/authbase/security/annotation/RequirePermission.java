package com.authbase.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for permission-based authorization.
 * Can be used on controller methods to require specific permissions.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

  /**
   * The permission name required to access this method.
   * 
   * @return permission name
   */
  String value();

  /**
   * Optional: Additional permissions that would also grant access.
   * 
   * @return array of alternative permission names
   */
  String[] orPermissions() default {};

  /**
   * Optional: Whether all specified permissions are required (AND logic).
   * If false, any permission will grant access (OR logic).
   * 
   * @return true if all permissions are required, false if any permission is
   *         sufficient
   */
  boolean requireAll() default false;
}