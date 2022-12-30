package com.syberry.bakery.service.impl;

import com.syberry.bakery.converter.RoleConverter;
import com.syberry.bakery.converter.UserConverter;
import com.syberry.bakery.dto.RoleDto;
import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.dto.UserDto;
import com.syberry.bakery.entity.Role;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.RoleRepository;
import com.syberry.bakery.repository.UserRepository;
import com.syberry.bakery.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleConverter roleConverter;
    private final UserConverter userConverter;

    @Override
    @Transactional
    public UserDto addRole(Long userId, Long roleId) {
        User user = userRepository.findByIdAndIsBlockedFalse(userId).orElseThrow(() -> new EntityNotFoundException("User is not found"));
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Role is not found"));
        List<Role> roles = user.getRoles();
        roles.add(role);
        user.setRoles(roles);
        return userConverter.convertToDto(userRepository.findByIdAndIsBlockedFalse(userId).get());
    }

    @Override
    @Transactional
    public UserDto removeRole(Long userId, Long roleId) {
        User user = userRepository.findByIdAndIsBlockedFalse(userId).orElseThrow(() -> new EntityNotFoundException("User is not found"));
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Role is not found"));
        List<Role> roles = user.getRoles();

        if (role.getRoleName().equals(RoleName.ROLE_ADMIN) && userRepository.countAllByBlockedIsFalseAndRoleIn(List.of(RoleName.ROLE_ADMIN)) == 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Last admin can't be deleted");
        } else if (roles.contains(role)) {
            if (roles.size() == 1) {
                roles = List.of(roleRepository.findByRoleName(RoleName.ROLE_USER).orElseThrow(() -> new EntityNotFoundException("Role is not found")));
            } else {
                roles.remove(role);
            }
        }
        user.setRoles(roles);
        return userConverter.convertToDto(userRepository.findByIdAndIsBlockedFalse(userId).get());
    }

    @Override
    public List<RoleDto> getRoles() {
        return roleConverter.convertToDtos(roleRepository.findAll());
    }
}
