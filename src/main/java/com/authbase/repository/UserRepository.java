package com.authbase.repository;

import com.authbase.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides custom query methods for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Find user by username (optional field).
   * 
   * @param username the username to search for
   * @return Optional containing the user if found
   */
  Optional<User> findByUsername(String username);

  /**
   * Find user by email.
   * 
   * @param email the email to search for
   * @return Optional containing the user if found
   */
  Optional<User> findByEmail(String email);

  /**
   * Check if user exists by username (optional field).
   * 
   * @param username the username to check
   * @return true if user exists, false otherwise
   */
  boolean existsByUsername(String username);

  /**
   * Check if user exists by email.
   * 
   * @param email the email to check
   * @return true if user exists, false otherwise
   */
  boolean existsByEmail(String email);

  /**
   * Find user by username or email.
   * 
   * @param usernameOrEmail username (optional) or email to search for
   * @return Optional containing the user if found
   */
  @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
  Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

  /**
   * Find enabled users only.
   * 
   * @return list of enabled users
   */
  @Query("SELECT u FROM User u WHERE u.isEnabled = true")
  java.util.List<User> findEnabledUsers();

  /**
   * Find users by role name.
   * 
   * @param roleName the role name to search for
   * @return list of users with the specified role
   */
  @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
  java.util.List<User> findByRoleName(@Param("roleName") String roleName);
}