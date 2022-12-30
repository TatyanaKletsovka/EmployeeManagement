package com.syberry.bakery.service;

import com.syberry.bakery.dto.SignUpRequestDto;
import com.syberry.bakery.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserDto> getAllUsers(Pageable pageable, String firstName, String lastName, String email, String role);
    UserDto getUserById(Long id);
    UserDto createUser(SignUpRequestDto signUpRequestDto);
    UserDto updateUser(UserDto userDto);
    void disableUser(Long id);
}
