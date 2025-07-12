package com.authbase.security.aspect;

import com.authbase.security.annotation.RequirePermission;
import com.authbase.security.service.PermissionEvaluatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Aspect for handling permission-based authorization using @RequirePermission
 * annotation.
 * Intercepts method calls and checks if the current user has the required
 * permissions.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PermissionAspect {

  private final PermissionEvaluatorService permissionEvaluatorService;

  /**
   * Intercept methods annotated with @RequirePermission and check permissions.
   * 
   * @param joinPoint         the join point
   * @param requirePermission the permission annotation
   */
  @Before("@annotation(requirePermission)")
  public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
    String methodName = joinPoint.getSignature().getName();
    String className = joinPoint.getTarget().getClass().getSimpleName();

    log.debug("Checking permission for {}.{}: {}", className, methodName, requirePermission.value());

    boolean hasPermission = false;

    if (requirePermission.requireAll()) {
      // Check if user has ALL permissions (AND logic)
      String[] allPermissions = combinePermissions(requirePermission.value(), requirePermission.orPermissions());
      hasPermission = permissionEvaluatorService.hasAllPermissions(allPermissions);
    } else {
      // Check if user has ANY permission (OR logic)
      String[] anyPermissions = combinePermissions(requirePermission.value(), requirePermission.orPermissions());
      hasPermission = permissionEvaluatorService.hasAnyPermission(anyPermissions);
    }

    if (!hasPermission) {
      String requiredPermissions = String.join(requirePermission.requireAll() ? " AND " : " OR ",
          combinePermissions(requirePermission.value(), requirePermission.orPermissions()));

      log.warn("Access denied for {}.{}: Required permissions: {}",
          className, methodName, requiredPermissions);

      throw new AccessDeniedException(
          String.format("Access denied. Required permissions: %s", requiredPermissions));
    }

    log.debug("Permission check passed for {}.{}", className, methodName);
  }

  /**
   * Combine the main permission with alternative permissions.
   * 
   * @param mainPermission         the main permission
   * @param alternativePermissions the alternative permissions
   * @return combined array of permissions
   */
  private String[] combinePermissions(String mainPermission, String[] alternativePermissions) {
    String[] allPermissions = new String[alternativePermissions.length + 1];
    allPermissions[0] = mainPermission;
    System.arraycopy(alternativePermissions, 0, allPermissions, 1, alternativePermissions.length);
    return allPermissions;
  }
}