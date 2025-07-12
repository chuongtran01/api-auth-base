package com.authbase.service;

/**
 * Service interface for email operations.
 * Handles sending various types of emails for user notifications.
 */
public interface EmailService {

  /**
   * Send email verification link to user.
   * 
   * @param email             user email
   * @param username          username
   * @param verificationToken verification token
   * @return true if email sent successfully
   */
  boolean sendVerificationEmail(String email, String username, String verificationToken);

  /**
   * Send password reset email to user.
   * 
   * @param email      user email
   * @param username   username
   * @param resetToken password reset token
   * @return true if email sent successfully
   */
  boolean sendPasswordResetEmail(String email, String username, String resetToken);

  /**
   * Send welcome email to newly registered user.
   * 
   * @param email     user email
   * @param username  username
   * @param firstName first name (optional)
   * @return true if email sent successfully
   */
  boolean sendWelcomeEmail(String email, String username, String firstName);

  /**
   * Send account lockout notification email.
   * 
   * @param email         user email
   * @param username      username
   * @param lockoutReason reason for lockout
   * @return true if email sent successfully
   */
  boolean sendAccountLockoutEmail(String email, String username, String lockoutReason);

  /**
   * Send security alert email.
   * 
   * @param email     user email
   * @param username  username
   * @param alertType type of security alert
   * @param details   alert details
   * @return true if email sent successfully
   */
  boolean sendSecurityAlertEmail(String email, String username, String alertType, String details);
}