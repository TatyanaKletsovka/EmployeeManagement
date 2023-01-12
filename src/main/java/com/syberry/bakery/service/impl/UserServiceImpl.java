package com.syberry.bakery.service.impl;

import com.google.common.cache.LoadingCache;
import com.syberry.bakery.converter.RoleConverter;
import com.syberry.bakery.converter.UserConverter;
import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.dto.SignUpRequestDto;
import com.syberry.bakery.dto.UserDto;
import com.syberry.bakery.dto.UsersFilterDto;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.repository.UserRepository;
import com.syberry.bakery.service.EmailService;
import com.syberry.bakery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final RoleConverter roleConverter;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final LoadingCache<String, String> oneTimePasswordCache;

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Page<UserDto> getAllUsers(Pageable pageable, UsersFilterDto usersFilterDto) {
        List<RoleName> roles = usersFilterDto.getRole() == null || usersFilterDto.getRole().isEmpty() ?
                Arrays.stream(RoleName.values()).toList() : List.of(roleConverter.convertToRoleName(usersFilterDto.getRole()));
        return userRepository.findAllByFiltering(usersFilterDto.getFirstName(), usersFilterDto.getLastName(), usersFilterDto.getEmail(), roles, pageable)
                .map(userConverter::convertToDto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public UserDto getUserById(Long id) {
        return userConverter.convertToDto(userRepository.findByIdAndIsBlockedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User is not found")));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto createUser(SignUpRequestDto signUpRequestDto) {
        signUpRequestDto.setPassword(encoder.encode(signUpRequestDto.getPassword()));
        User user = userConverter.convertToEntity(signUpRequestDto);
        user.setCreatedAt(LocalDateTime.now());
        return userConverter.convertToDto(userRepository.save(user));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateUser(UserDto userDto) {
        User user = userRepository.findByIdAndIsBlockedFalse(userDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));

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
    @PreAuthorize("hasRole('ADMIN')")
    public void disableUser(Long id) {
        User user = userRepository.findByIdAndIsBlockedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
        user.setIsBlocked(true);
        user.setDisabledAt(LocalDateTime.now());
    }

    @Override
    public void requestPasswordResettingEmail(String email) {
        User user = userRepository.findByEmailAndIsBlockedFalse(email).orElseThrow(() -> new EntityNotFoundException("User is not found"));
        triggerToResettingPassword(user);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public void requestPasswordResettingEmail(Long userId) {
        User user = userRepository.findByIdAndIsBlockedFalse(userId)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
        triggerToResettingPassword(user);
    }

    private void triggerToResettingPassword(User user) {
        String token = generateResetPasswordToken();
        oneTimePasswordCache.put(user.getEmail(), token);
        emailService.sendResetPasswordEmail(token, user.getEmail());
    }

    private String generateResetPasswordToken() {
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder token = new StringBuilder();
        Random rnd = new Random();
        while (token.length() < 18) {
            int index = (int) (rnd.nextFloat() * CHARS.length());
            token.append(CHARS.charAt(index));
        }
        return token.toString();
    }
}
