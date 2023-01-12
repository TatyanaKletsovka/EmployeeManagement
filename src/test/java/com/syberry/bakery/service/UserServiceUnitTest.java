package com.syberry.bakery.service;

import com.syberry.bakery.converter.UserConverter;
import com.syberry.bakery.dto.SignUpRequestDto;
import com.syberry.bakery.dto.UserDto;
import com.syberry.bakery.dto.UsersFilterDto;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.UserRepository;
import com.syberry.bakery.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceUnitTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserConverter userConverter;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder encoder;

    @Test
    void should_SuccessfullyReturnAllUsers() {
        when(userRepository.findAllByFiltering(any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));
        assertThat(userService.getAllUsers(PageRequest.of(0, 20), new UsersFilterDto())).isEqualTo(new PageImpl<>(List.of()));
    }

    @Test
    void should_SuccessfullyReturnUserById() {
        when(userRepository.findByIdAndIsBlockedFalse(any())).thenReturn(Optional.of(new User()));
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        when(userConverter.convertToDto(any(User.class))).thenReturn(userDto);
        UserDto user = userService.getUserById(1L);
        UserDto expectedUser = new UserDto();
        expectedUser.setId(userDto.getId());
        assertThat(user).isEqualTo(expectedUser);
    }

    @Test
    void should_ThrowError_WhenGettingByIdNoneExistingUser() {
        when(userRepository.findByIdAndIsBlockedFalse(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void should_SuccessfullyCreateUser() {
        when(userConverter.convertToEntity(any(SignUpRequestDto.class))).thenReturn(new User());
        when(userRepository.save(any())).thenReturn(new User());
        when(userConverter.convertToDto(any())).thenReturn(new UserDto());
        when(encoder.encode(any())).thenReturn("encoded");
        assertThat(userService.createUser(new SignUpRequestDto())).isEqualTo(new UserDto());
    }

    @Test
    void should_SuccessfullyUpdateUser() {
        when(userRepository.findByIdAndIsBlockedFalse(any())).thenReturn(Optional.of(new User()));
        when(userConverter.convertToDto(any())).thenReturn(new UserDto());
        assertThat(userService.updateUser(new UserDto())).isEqualTo(new UserDto());

    }

    @Test
    void should_ThrowError_When_UpdatingNoneExistingUser() {
        when(userRepository.findByIdAndIsBlockedFalse(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(new UserDto()));
    }

    @Test
    void should_SuccessfullyDisableUser() {
        User user = new User();
        when(userRepository.findByIdAndIsBlockedFalse(any())).thenReturn(Optional.of(user));
        userService.disableUser(any());
        assertThat(user.getIsBlocked()).isTrue();
    }

    @Test
    void should_ThrowError_When_DisablingNoneExistingUser() {
        when(userRepository.findByIdAndIsBlockedFalse(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.disableUser(any()));
    }

}
