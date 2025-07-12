package com.authbase.repository;

import com.authbase.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Permission entity operations.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

  /**
   * Find permission by name.
   * 
   * @param name the permission name
   * @return Optional containing the permission if found
   */
  Optional<Permission> findByName(String name);

  /**
   * Check if permission exists by name.
   * 
   * @param name the permission name
   * @return true if permission exists, false otherwise
   */
  boolean existsByName(String name);
}