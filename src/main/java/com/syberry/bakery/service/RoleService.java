package com.syberry.bakery.service;

import com.syberry.bakery.dto.RoleDto;
import com.syberry.bakery.dto.UserDto;

import java.util.List;

public interface RoleService {
    UserDto addRole(Long userId, Long roleId);
    UserDto removeRole(Long userId, Long roleId);
    List<RoleDto> getRoles();
}
