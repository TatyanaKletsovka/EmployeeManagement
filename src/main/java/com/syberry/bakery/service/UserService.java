package com.syberry.bakery.service;

import com.syberry.bakery.dto.SignUpRequestDto;
import com.syberry.bakery.dto.UserDto;
import com.syberry.bakery.dto.UsersFilterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserDto> getAllUsers(Pageable pageable, UsersFilterDto usersFilterDto);

    UserDto getUserById(Long id);

    UserDto createUser(SignUpRequestDto signUpRequestDto);

    UserDto updateUser(UserDto userDto);

    void disableUser(Long id);

    void requestPasswordResettingEmail(String email);

    void requestPasswordResettingEmail(Long userId);
}
