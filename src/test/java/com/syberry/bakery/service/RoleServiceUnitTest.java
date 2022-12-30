package com.syberry.bakery.service;

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
import com.syberry.bakery.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RoleServiceUnitTest {
    @InjectMocks
    private RoleServiceImpl roleService;
    @Mock
    private RoleConverter roleConverter;
    @Mock
    private UserConverter userConverter;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    @Test
    public void should_AddRoleToUser_When_EnteredCorrectData() {
        Role userRole = new Role(4L, RoleName.ROLE_USER);
        when(roleRepository.findById(4L)).thenReturn(Optional.of(userRole));
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setRoles(new ArrayList<>(List.of(new Role(2L, RoleName.ROLE_HR))));
        when(userRepository.findByIdAndIsBlockedFalse(1L)).thenReturn(Optional.of(savedUser));
        UserDto userDto = new UserDto();
        userDto.setRoles(new ArrayList<>(List.of(RoleName.ROLE_USER.name(), RoleName.ROLE_HR.name())));
        when(userConverter.convertToDto(any(User.class))).thenReturn(userDto);
        UserDto user = roleService.addRole(1L, 4L);
        userDto.setId(1L);
        assertThat(user).isEqualTo(userDto);
    }

    @Test
    public void should_ThrowError_When_AddingRoleToNoneExistingUser() {
        Role userRole = new Role(4L, RoleName.ROLE_USER);
        when(roleRepository.findById(4L)).thenReturn(Optional.of(userRole));
        when(userRepository.findByIdAndIsBlockedFalse(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> roleService.addRole(1L, 4L));
    }

    @Test
    public void should_ThrowError_When_AddingNoneExistingRoleToUser() {
        when(roleRepository.findById(4L)).thenReturn(Optional.empty());
        when(userRepository.findByIdAndIsBlockedFalse(1L)).thenReturn(Optional.of(new User()));
        assertThrows(EntityNotFoundException.class, () -> roleService.addRole(1L, 4L));
    }

    @Test
    public void should_SuccessfullyRemoveRoleFromUser() {
        Role userRole = new Role(4L, RoleName.ROLE_USER);
        when(roleRepository.findById(4L)).thenReturn(Optional.of(userRole));
        User user = new User();
        user.setId(1L);
        user.setRoles(new ArrayList<>(List.of(new Role(2L, RoleName.ROLE_HR), userRole)));
        when(userRepository.findByIdAndIsBlockedFalse(1L)).thenReturn(Optional.of(user));
        UserDto userDto = new UserDto();
        userDto.setRoles(new ArrayList<>(List.of(RoleName.ROLE_HR.name())));
        when(userConverter.convertToDto(any(User.class))).thenReturn(userDto);
        when(userRepository.countAllByBlockedIsFalseAndRoleIn(List.of(RoleName.ROLE_ADMIN))).thenReturn(1L);
        when(roleRepository.findByRoleName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        UserDto savedUser = roleService.removeRole(1L, 4L);
        userDto.setId(1L);
        assertThat(savedUser).isEqualTo(userDto);
    }

    @Test
    public void should_ThrowError_When_RemovingNoneExistingRoleFromUser() {
        when(roleRepository.findById(4L)).thenReturn(Optional.empty());
        when(userRepository.findByIdAndIsBlockedFalse(1L)).thenReturn(Optional.of(new User()));
        assertThrows(EntityNotFoundException.class, () -> roleService.removeRole(1L, 4L));
    }

    @Test
    public void should_ThrowError_When_RemovingRoleFromNoneExistingUser() {
        Role userRole = new Role(4L, RoleName.ROLE_USER);
        when(roleRepository.findById(4L)).thenReturn(Optional.of(userRole));
        when(userRepository.findByIdAndIsBlockedFalse(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> roleService.removeRole(1L, 4L));
    }

    @Test
    public void should_ThrowError_When_RemovingLastAdmin() {
        Role adminRole = new Role(1L, RoleName.ROLE_ADMIN);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));
        User user = new User();
        user.setId(1L);
        user.setRoles(new ArrayList<>(List.of(adminRole)));
        when(userRepository.findByIdAndIsBlockedFalse(1L)).thenReturn(Optional.of(user));
        when(userRepository.countAllByBlockedIsFalseAndRoleIn(List.of(RoleName.ROLE_ADMIN))).thenReturn(1L);
        assertThrows(ResponseStatusException.class, () -> roleService.removeRole(1L, 1L));
    }

    @Test
    public void should_SuccessfullyReturnAllRoles() {
        when(roleConverter.convertToDtos(any())).thenReturn(new ArrayList<>(List.of(new RoleDto())));
        assertThat(roleService.getRoles().get(0)).isEqualTo(new RoleDto());
    }
}
