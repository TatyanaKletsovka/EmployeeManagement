package com.syberry.bakery.converter;

import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.dto.UserDto;
import com.syberry.bakery.entity.Role;
import com.syberry.bakery.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.as;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserConverterUnitTest {
    @InjectMocks
    private UserConverter userConverter;
    @Mock
    private RoleConverter roleConverter;

    @Test
    public void should_SuccessfullyConvertToDtos() {
        when(roleConverter.convertToStringRoles(any())).thenReturn(new ArrayList<>(List.of(RoleName.ROLE_USER.name())));
        User user = new User();
        user.setId(1L);
        user.setFirstName("test");
        user.setLastName("test");
        user.setEmail("test@mail.com");
        user.setRoles(new ArrayList<>(List.of(new Role(1L, RoleName.ROLE_USER))));
        List<UserDto> resultDto = userConverter.convertToDtos(new ArrayList<>(List.of(user)));
        UserDto expectedDto = new UserDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),new ArrayList<>(List.of(RoleName.ROLE_USER.name())));
        resultDto.get(0).equals(expectedDto);
        assertThat(resultDto.get(0)).isEqualTo(expectedDto);
    }

    @Test
    public void should_SuccessfullyConvertToDto() {
        when(roleConverter.convertToStringRoles(any())).thenReturn(new ArrayList<>(List.of(RoleName.ROLE_USER.name())));
        User user = new User();
        user.setId(1L);
        user.setFirstName("test");
        user.setLastName("test");
        user.setEmail("test@mail.com");
        user.setRoles(new ArrayList<>(List.of(new Role(1L, RoleName.ROLE_USER))));
        UserDto resultDto = userConverter.convertToDto(user);
        UserDto expectedDto = new UserDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),new ArrayList<>(List.of(RoleName.ROLE_USER.name())));
        assertThat(resultDto).isEqualTo(expectedDto);
    }

    @Test
    public void should_SuccessfullyConvertUserDtoToEntity() {
        when(roleConverter.convertToEntityRoles(any())).thenReturn(new ArrayList<>(List.of(new Role(1L, RoleName.ROLE_USER))));
        UserDto user = new UserDto();
        user.setFirstName("test");
        user.setLastName("test");
        user.setEmail("test@mail.com");
        user.setRoles(new ArrayList<>(List.of(RoleName.ROLE_USER.name())));
        User resultUser = userConverter.convertToEntity(user);
        User expectedUser = new User();
        expectedUser.setFirstName(user.getFirstName());
        expectedUser.setLastName(user.getLastName());
        expectedUser.setEmail(user.getEmail());
        expectedUser.setRoles(new ArrayList<>(List.of(new Role(1L, RoleName.ROLE_USER))));
        assertThat(resultUser).isEqualTo(expectedUser);
    }

    @Test
    public void should_SuccessfullyConvertSignUpRequestDtoToEntity() {
        when(roleConverter.convertToEntityRoles(any())).thenReturn(new ArrayList<>(List.of(new Role(1L, RoleName.ROLE_USER))));
        UserDto user = new UserDto();
        user.setFirstName("test");
        user.setLastName("test");
        user.setEmail("test@mail.com");
        user.setRoles(new ArrayList<>(List.of(RoleName.ROLE_USER.name())));
        User resultUser = userConverter.convertToEntity(user);
        User expectedUser = new User();
        expectedUser.setFirstName(user.getFirstName());
        expectedUser.setLastName(user.getLastName());
        expectedUser.setEmail(user.getEmail());
        expectedUser.setRoles(new ArrayList<>(List.of(new Role(1L, RoleName.ROLE_USER))));
        assertThat(resultUser).isEqualTo(expectedUser);
    }
}
