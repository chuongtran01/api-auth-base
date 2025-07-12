package com.authbase.service.impl;

import com.authbase.entity.Permission;
import com.authbase.entity.Role;
import com.authbase.entity.User;
import com.authbase.repository.PermissionRepository;
import com.authbase.repository.RoleRepository;
import com.authbase.repository.UserRepository;
import com.authbase.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of RoleService interface.
 * Handles role creation, permission assignment, and user-role management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final UserRepository userRepository;

  @Override
  public Role createRole(String name, String description) {
    log.info("Creating new role: {}", name);

    if (roleRepository.findByName(name).isPresent()) {
      throw new IllegalArgumentException("Role with name " + name + " already exists");
    }

    Role role = new Role(name, description);
    Role savedRole = roleRepository.save(role);

    log.info("Role created successfully: {}", savedRole.getName());
    return savedRole;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Role> findByName(String name) {
    log.debug("Finding role by name: {}", name);
    return roleRepository.findByName(name);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Role> findById(Long id) {
    log.debug("Finding role by ID: {}", id);
    return roleRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Role> findAllRoles() {
    log.debug("Finding all roles");
    return roleRepository.findAll();
  }

  @Override
  public Role updateRoleDescription(Long roleId, String description) {
    log.info("Updating description for role ID: {}", roleId);

    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

    role.setDescription(description);
    Role updatedRole = roleRepository.save(role);

    log.info("Role description updated successfully: {}", updatedRole.getName());
    return updatedRole;
  }

  @Override
  public boolean deleteRole(Long roleId) {
    log.info("Deleting role ID: {}", roleId);

    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

    // Check if role has users
    if (!role.getUsers().isEmpty()) {
      throw new IllegalArgumentException("Cannot delete role with assigned users");
    }

    roleRepository.delete(role);
    log.info("Role deleted successfully: {}", role.getName());
    return true;
  }

  @Override
  public Role addPermissionToRole(Long roleId, Long permissionId) {
    log.info("Adding permission {} to role {}", permissionId, roleId);

    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

    Permission permission = permissionRepository.findById(permissionId)
        .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + permissionId));

    role.addPermission(permission);
    Role updatedRole = roleRepository.save(role);

    log.info("Permission added to role successfully: {} -> {}", permission.getName(), role.getName());
    return updatedRole;
  }

  @Override
  public Role removePermissionFromRole(Long roleId, Long permissionId) {
    log.info("Removing permission {} from role {}", permissionId, roleId);

    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

    Permission permission = permissionRepository.findById(permissionId)
        .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + permissionId));

    role.removePermission(permission);
    Role updatedRole = roleRepository.save(role);

    log.info("Permission removed from role successfully: {} -> {}", permission.getName(), role.getName());
    return updatedRole;
  }

  @Override
  public User assignRoleToUser(Long userId, Long roleId) {
    log.info("Assigning role {} to user {}", roleId, userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

    user.addRole(role);
    User updatedUser = userRepository.save(user);

    log.info("Role assigned to user successfully: {} -> {}", role.getName(), user.getEmail());
    return updatedUser;
  }

  @Override
  public User removeRoleFromUser(Long userId, Long roleId) {
    log.info("Removing role {} from user {}", roleId, userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

    user.removeRole(role);
    User updatedUser = userRepository.save(user);

    log.info("Role removed from user successfully: {} -> {}", role.getName(), user.getEmail());
    return updatedUser;
  }

  @Override
  @Transactional(readOnly = true)
  public Set<Permission> getRolePermissions(Long roleId) {
    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

    return role.getPermissions();
  }

  @Override
  @Transactional(readOnly = true)
  public Set<Role> getUserRoles(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    return user.getRoles();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean userHasRole(Long userId, String roleName) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    return user.getRoles().stream()
        .anyMatch(role -> role.getName().equals(roleName));
  }

  @Override
  @Transactional(readOnly = true)
  public boolean roleHasPermission(Long roleId, String permissionName) {
    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

    return role.getPermissions().stream()
        .anyMatch(permission -> permission.getName().equals(permissionName));
  }

  @Override
  @Transactional(readOnly = true)
  public long getRoleCount() {
    return roleRepository.count();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByName(String name) {
    return roleRepository.findByName(name).isPresent();
  }
}