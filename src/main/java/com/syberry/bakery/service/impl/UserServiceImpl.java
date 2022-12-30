package com.syberry.bakery.service.impl;

import com.syberry.bakery.converter.RoleConverter;
import com.syberry.bakery.converter.UserConverter;
import com.syberry.bakery.dto.SignUpRequestDto;
import com.syberry.bakery.dto.UserDto;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.UserRepository;
import com.syberry.bakery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final RoleConverter roleConverter;

    @Override
    public Page<UserDto> getAllUsers(Pageable pageable, String firstName, String lastName, String email, String role) {
        List<RoleName> roles;
        if (role == null || role.isEmpty()) {
            roles = Arrays.stream(RoleName.values()).toList();
        } else {
            roles = List.of(roleConverter.convertToRoleName(role));
        }
        return userRepository.findAllByFiltering(firstName, lastName, email, roles, pageable).map(userConverter::convertToDto);
    }

    @Override
    public UserDto getUserById(Long id) {
        return userConverter.convertToDto(userRepository.findByIdAndIsBlockedFalse(id).orElseThrow(() -> new EntityNotFoundException("User is not found")));
    }

    @Override
    public UserDto createUser(SignUpRequestDto signUpRequestDto) {
        // password encoding will be implemented in feature 'Authentication and Authorization', because therefor needed security dependency
        User user = userConverter.convertToEntity(signUpRequestDto);
        user.setCreatedAt(LocalDateTime.now());
        return userConverter.convertToDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto updateUser(UserDto userDto) {
        User user = userRepository.findByIdAndIsBlockedFalse(userDto.getId()).orElseThrow(() -> new EntityNotFoundException("User is not found"));

        if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()) {
            user.setEmail(userDto.getEmail().toLowerCase());
        }
        if (userDto.getFirstName() != null && !userDto.getFirstName().isEmpty()) {
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null && !userDto.getLastName().isEmpty()) {
            user.setLastName(userDto.getLastName());
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userConverter.convertToDto(userRepository.findByIdAndIsBlockedFalse(userDto.getId()).get());
    }

    @Override
    @Transactional
    public void disableUser(Long id) {
        User user = userRepository.findByIdAndIsBlockedFalse(id).orElseThrow(() -> new EntityNotFoundException("User is not found"));
        user.setIsBlocked(true);
        user.setDisabledAt(LocalDateTime.now());
    }
}
