package com.syberry.bakery.controller;

import com.syberry.bakery.dto.SignUpRequestDto;
import com.syberry.bakery.dto.UserDto;
import com.syberry.bakery.dto.UsersFilterDto;
import com.syberry.bakery.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public Page<UserDto> getAllUsers(Pageable pageable, UsersFilterDto usersFilterDto) {
        log.info("Retrieving all users");
        return userService.getAllUsers(pageable, usersFilterDto);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable("id") Long id) {
        log.info("Retrieving user with id: {}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        log.info("Creating new user");
        return userService.createUser(signUpRequestDto);
    }

    @PutMapping("/{id}")
    public UserDto updateUserById(@PathVariable("id") Long id, @RequestBody UserDto userDto) {
        log.info("Updating user with id: {}", id);
        userDto.setId(id);
        return userService.updateUser(userDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableUserById(@PathVariable("id") Long id) {
        log.info("Deleting user with id: {}", id);
        userService.disableUser(id);
    }

}
