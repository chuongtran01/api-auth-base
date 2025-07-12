package com.authbase.service;

import com.authbase.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for user management operations.
 * Handles user registration, authentication, profile updates, and deletion.
 */
public interface UserService {

  /**
   * Register a new user.
   * 
   * @param email     user email (required)
   * @param password  plain text password (required)
   * @param username  username (optional)
   * @param firstName first name (optional)
   * @param lastName  last name (optional)
   * @return created user
   * @throws IllegalArgumentException if user already exists
   */
  User registerUser(String email, String password, String username, String firstName, String lastName);

  /**
   * Register a new user with minimal information.
   * 
   * @param email    user email (required)
   * @param password plain text password (required)
   * @param username username (optional)
   * @return created user
   */
  User registerUser(String email, String password, String username);

  /**
   * Find user by username.
   * 
   * @param username username to search for
   * @return Optional containing user if found
   */
  Optional<User> findByUsername(String username);

  /**
   * Find user by email.
   * 
   * @param email email to search for
   * @return Optional containing user if found
   */
  Optional<User> findByEmail(String email);

  /**
   * Find user by ID.
   * 
   * @param id user ID
   * @return Optional containing user if found
   */
  Optional<User> findById(Long id);

  /**
   * Get all users.
   * 
   * @return list of all users
   */
  List<User> findAllUsers();

  /**
   * Update user profile information.
   * 
   * @param userId    user ID
   * @param firstName new first name
   * @param lastName  new last name
   * @return updated user
   * @throws IllegalArgumentException if user not found
   */
  User updateProfile(Long userId, String firstName, String lastName);

  /**
   * Change user password.
   * 
   * @param userId          user ID
   * @param currentPassword current password
   * @param newPassword     new password
   * @return true if password changed successfully
   * @throws IllegalArgumentException if current password is incorrect or user not
   *                                  found
   */
  boolean changePassword(Long userId, String currentPassword, String newPassword);

  /**
   * Update user's email verification status.
   * 
   * @param userId     user ID
   * @param isVerified verification status
   * @return updated user
   */
  User updateEmailVerificationStatus(Long userId, boolean isVerified);

  /**
   * Enable or disable user account.
   * 
   * @param userId  user ID
   * @param enabled enabled status
   * @return updated user
   */
  User updateAccountStatus(Long userId, boolean enabled);

  /**
   * Update user's last login timestamp.
   * 
   * @param userId user ID
   * @return updated user
   */
  User updateLastLogin(Long userId);

  /**
   * Delete user account.
   * 
   * @param userId user ID
   * @return true if deleted successfully
   */
  boolean deleteUser(Long userId);

  /**
   * Check if user exists by email.
   * 
   * @param email email to check
   * @return true if user exists
   */
  boolean existsByEmail(String email);

  /**
   * Check if user exists by username.
   * 
   * @param username username to check
   * @return true if user exists
   */
  boolean existsByUsername(String username);

  /**
   * Get user count.
   * 
   * @return total number of users
   */
  long getUserCount();
}