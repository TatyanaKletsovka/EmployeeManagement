package com.syberry.bakery.controller;

import com.syberry.bakery.dto.RoleDto;
import com.syberry.bakery.dto.UserDto;
import com.syberry.bakery.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping("/users/{userId}/roles/{roleId}")
    public UserDto addRoleToUser(@PathVariable Long roleId, @PathVariable Long userId) {
        log.info("Adding role with id: {} to user with id: {}", roleId, userId);
        return roleService.addRole(userId, roleId);
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public UserDto removeRole(@PathVariable Long roleId, @PathVariable Long userId) {
        log.info("Removing role with id: {} from user with id: {}", roleId, userId);
        return roleService.removeRole(userId, roleId);
    }

    @GetMapping("/roles")
    public List<RoleDto> getRoles() {
        log.info("Retrieving all roles from database");
        return roleService.getRoles();
    }

    @GetMapping("/roles/{id}")
    public RoleDto getRolesById(@PathVariable Long id) {
        log.info("Retrieving role by id from database");
        return roleService.getRoleById(id);
    }
}
