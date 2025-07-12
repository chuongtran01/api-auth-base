package com.authbase.mapper;

import com.authbase.dto.UserDto;
import com.authbase.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for converting between User entities and UserDto objects.
 * Handles the mapping of roles from Role entities to role names.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

  /**
   * Map User entity to UserDto.
   * 
   * @param user the user entity
   * @return UserDto
   */
  @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToRoleNames")
  UserDto toDto(User user);

  /**
   * Map UserDto to User entity.
   * Note: This is a one-way mapping for now, as we don't want to create entities
   * from DTOs in most cases.
   * 
   * @param userDto the user DTO
   * @return User entity (with roles not mapped)
   */
  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "refreshTokens", ignore = true)
  @Mapping(target = "password", ignore = true)
  User toEntity(UserDto userDto);

  /**
   * Map a set of User entities to UserDto objects.
   * 
   * @param users the set of user entities
   * @return Set of UserDto objects
   */
  Set<UserDto> toDtoSet(Set<User> users);

  /**
   * Convert Role entities to role names.
   * 
   * @param roles the set of Role entities
   * @return Set of role names as strings
   */
  @Named("rolesToRoleNames")
  default Set<String> rolesToRoleNames(Set<com.authbase.entity.Role> roles) {
    if (roles == null) {
      return Set.of();
    }
    return roles.stream()
        .map(com.authbase.entity.Role::getName)
        .collect(Collectors.toSet());
  }
}