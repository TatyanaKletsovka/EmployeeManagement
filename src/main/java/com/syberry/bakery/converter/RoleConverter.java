package com.syberry.bakery.converter;

import com.syberry.bakery.dto.RoleDto;
import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.entity.Role;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.exception.InvalidArgumentTypeException;
import com.syberry.bakery.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RoleConverter {
    private final RoleRepository roleRepository;
    private final static String PREFIX = "ROLE_";

    public RoleDto convertToDto(Role role) {
        return RoleDto.builder()
                .id(role.getId())
                .name(role.getRoleName().name())
                .build();
    }

    public List<RoleDto> convertToDtos(List<Role> roles) {
        List<RoleDto> dtoRoles = new ArrayList<>();
        roles.forEach(role -> {
            dtoRoles.add(RoleDto.builder()
                    .id(role.getId())
                    .name(role.getRoleName().name())
                    .build());
        });
        return dtoRoles;
    }

    public Role convertToEntity(RoleDto roleDto) {
        return new Role(roleDto.getId(), convertToRoleName(roleDto.getName()));
    }

    public List<Role> convertToEntities(List<RoleDto> roles) {
        List<Role> entRoles = new ArrayList<>();
        roles.forEach(role -> {
            entRoles.add(new Role(role.getId(), convertToRoleName(role.getName())));
        });
        return entRoles;
    }

    public RoleName convertToRoleName(String role) throws EntityNotFoundException {
        try {
            role = role.toUpperCase();
            if (!role.startsWith(PREFIX)) {
                role = PREFIX + role;
            }
            return RoleName.valueOf(role);
        } catch (IllegalArgumentException ex) {
            throw new InvalidArgumentTypeException("Error while converting string role to enum role");
        }
    }

    public List<String> convertToStringRoles(Set<Role> entRoles) {
        List<String> roles = new ArrayList<>();
        entRoles.forEach(role -> {
            roles.add(role.getRoleName().name());
        });
        return roles;
    }

    public Set<Role> convertToEntityRoles(List<String> strRoles) {
        Set<Role> roles = new HashSet<>();
        if (strRoles == null) {
            Role userRole = roleRepository.findByRoleName(RoleName.ROLE_USER).get();
            roles.add(userRole);
        } else {
            strRoles.forEach(strRole -> {
                Role role = roleRepository.findByRoleName(convertToRoleName(strRole)).get();
                roles.add(role);
            });
        }
        return roles;
    }

}
