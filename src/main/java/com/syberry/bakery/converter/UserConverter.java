package com.syberry.bakery.converter;

import com.syberry.bakery.dto.SignUpRequestDto;
import com.syberry.bakery.dto.UserDto;
import com.syberry.bakery.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserConverter {
    private final RoleConverter roleConverter;

    public List<UserDto> convertToDtos(List<User> users) {
        List<UserDto> userDtos = new ArrayList<>();
        users.forEach(user -> {
            userDtos.add(UserDto.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .roles(roleConverter.convertToStringRoles(user.getRoles()))
                    .build());
        });
        return userDtos;
    }

    public UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(roleConverter.convertToStringRoles(user.getRoles()))
                .build();
    }

    public User convertToEntity(UserDto userDto) {
        User user = new User(userDto.getFirstName(), userDto.getLastName(), userDto.getEmail());
        user.setRoles(roleConverter.convertToEntityRoles(userDto.getRoles()));
        return user;
    }

    public User convertToEntity(SignUpRequestDto signUpRequestDto) {
        User user = new User(signUpRequestDto.getFirstName(),
                signUpRequestDto.getLastName(),
                signUpRequestDto.getEmail().toLowerCase(),
                signUpRequestDto.getPassword());
        user.setRoles(roleConverter.convertToEntityRoles(signUpRequestDto.getRoles()));
        return user;
    }
}
