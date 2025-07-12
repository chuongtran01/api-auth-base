package com.authbase.service.impl;

import com.authbase.entity.User;
import com.authbase.repository.UserRepository;
import com.authbase.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserService interface.
 * Handles user management operations including registration, profile updates,
 * and deletion.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public User registerUser(String email, String password, String username, String firstName, String lastName) {
    log.info("Registering new user: {}", email);

    // Check if user already exists by email
    if (userRepository.findByEmail(email).isPresent()) {
      throw new IllegalArgumentException("User with email " + email + " already exists");
    }

    // If username is provided, check uniqueness
    if (username != null && !username.isBlank() && userRepository.findByUsername(username).isPresent()) {
      throw new IllegalArgumentException("User with username " + username + " already exists");
    }

    // Create new user
    User user = new User(email, passwordEncoder.encode(password), username, firstName, lastName);
    user.setIsEnabled(true);
    user.setIsEmailVerified(false);

    User savedUser = userRepository.save(user);
    log.info("User registered successfully: {}", savedUser.getEmail());

    return savedUser;
  }

  @Override
  public User registerUser(String email, String password, String username) {
    return registerUser(email, password, username, null, null);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByUsername(String username) {
    log.debug("Finding user by username: {}", username);
    return userRepository.findByUsername(username);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    log.debug("Finding user by email: {}", email);
    return userRepository.findByEmail(email);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findById(Long id) {
    log.debug("Finding user by ID: {}", id);
    return userRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findAllUsers() {
    log.debug("Finding all users");
    return userRepository.findAll();
  }

  @Override
  public User updateProfile(Long userId, String firstName, String lastName) {
    log.info("Updating profile for user ID: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    user.setFirstName(firstName);
    user.setLastName(lastName);

    User updatedUser = userRepository.save(user);
    log.info("Profile updated successfully for user: {}", updatedUser.getEmail());

    return updatedUser;
  }

  @Override
  public boolean changePassword(Long userId, String currentPassword, String newPassword) {
    log.info("Changing password for user ID: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    // Verify current password
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new IllegalArgumentException("Current password is incorrect");
    }

    // Update password
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    log.info("Password changed successfully for user: {}", user.getEmail());
    return true;
  }

  @Override
  public User updateEmailVerificationStatus(Long userId, boolean isVerified) {
    log.info("Updating email verification status for user ID: {} to: {}", userId, isVerified);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    user.setIsEmailVerified(isVerified);
    User updatedUser = userRepository.save(user);

    log.info("Email verification status updated for user: {}", updatedUser.getEmail());
    return updatedUser;
  }

  @Override
  public User updateAccountStatus(Long userId, boolean enabled) {
    log.info("Updating account status for user ID: {} to: {}", userId, enabled);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    user.setIsEnabled(enabled);
    User updatedUser = userRepository.save(user);

    log.info("Account status updated for user: {} to: {}", updatedUser.getEmail(), enabled);
    return updatedUser;
  }

  @Override
  public User updateLastLogin(Long userId) {
    log.debug("Updating last login for user ID: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    user.setLastLoginAt(LocalDateTime.now());
    return userRepository.save(user);
  }

  @Override
  public boolean deleteUser(Long userId) {
    log.info("Deleting user account: {}", userId);

    if (!userRepository.existsById(userId)) {
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    userRepository.deleteById(userId);
    log.info("User account deleted successfully: {}", userId);
    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByEmail(String email) {
    return userRepository.findByEmail(email).isPresent();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByUsername(String username) {
    return userRepository.findByUsername(username).isPresent();
  }

  @Override
  @Transactional(readOnly = true)
  public long getUserCount() {
    return userRepository.count();
  }
}