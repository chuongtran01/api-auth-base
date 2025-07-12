package com.authbase.repository;

import com.authbase.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity operations.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

  /**
   * Find role by name.
   * 
   * @param name the role name
   * @return Optional containing the role if found
   */
  Optional<Role> findByName(String name);

  /**
   * Check if role exists by name.
   * 
   * @param name the role name
   * @return true if role exists, false otherwise
   */
  boolean existsByName(String name);
}