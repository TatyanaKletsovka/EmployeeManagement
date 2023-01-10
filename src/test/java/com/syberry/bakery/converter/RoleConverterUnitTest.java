package com.syberry.bakery.converter;

import com.syberry.bakery.dto.RoleDto;
import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.entity.Role;
import com.syberry.bakery.exception.InvalidArgumentTypeException;
import com.syberry.bakery.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RoleConverterUnitTest {
    @InjectMocks
    private RoleConverter roleConverter;
    @Mock
    private RoleRepository roleRepository;

    @Test
    void should_SuccessfullyConvertToDto() {
        assertThat(roleConverter.convertToDto(new Role(1L, RoleName.ROLE_USER)).getName())
                .isEqualTo(RoleName.ROLE_USER.name());
    }

    @Test
    void should_SuccessfullyConvertToDtos() {
        Role role = new Role(1L, RoleName.ROLE_USER);
        assertThat(roleConverter.convertToDtos(new ArrayList<>(List.of(role))).get(0).getName())
                .isEqualTo(RoleName.ROLE_USER.name());
    }

    @Test
    void should_SuccessfullyConvertToEntity() {
        assertThat(roleConverter.convertToEntity(new RoleDto(1L, RoleName.ROLE_USER.name())))
                .isEqualTo(new Role(1L, RoleName.ROLE_USER));
    }

    @Test
    void should_SuccessfullyConvertToEntities() {
        assertThat(roleConverter.convertToEntities(new ArrayList<>(List.of(new RoleDto(1L, RoleName.ROLE_USER.name())))))
                .contains(new Role(1L, RoleName.ROLE_USER));
    }

    @Test
    void should_SuccessfullyConvertToRoleName() {
        assertThat(roleConverter.convertToRoleName("user"))
                .isEqualTo(RoleName.ROLE_USER);
    }

    @Test
    void should_ThrowError_When_ConvertingInvalidRoleName() {
        assertThrows(InvalidArgumentTypeException.class, () -> roleConverter.convertToRoleName(""));
    }

    @Test
    void should_SuccessfullyConvertToStringRoles() {
        assertThat(roleConverter.convertToStringRoles(new HashSet<>(List.of(new Role(1L, RoleName.ROLE_USER)))))
                .contains("ROLE_USER");
    }

    @Test
    void should_SuccessfullyConvertToEntityRoles() {
        when(roleRepository.findByRoleName(any())).thenReturn(Optional.of(new Role(1L, RoleName.ROLE_USER)));
        assertThat(roleConverter.convertToEntityRoles(new ArrayList<>(List.of("USER"))))
                .contains(new Role(1L, RoleName.ROLE_USER));
    }

    @Test
    void should_SuccessfullyConvertToEntityRolesToDefaultRole_If_Null() {
        when(roleRepository.findByRoleName(any())).thenReturn(Optional.of(new Role(1L, RoleName.ROLE_USER)));
        assertThat(roleConverter.convertToEntityRoles(null)).contains(new Role(1L, RoleName.ROLE_USER));
    }
}
