package com.syberry.bakery.controller;

import com.syberry.bakery.dto.AuthResponse;
import com.syberry.bakery.dto.EmailVerificationDto;
import com.syberry.bakery.dto.LoginDto;
import com.syberry.bakery.dto.LoginRequestDto;
import com.syberry.bakery.dto.LoginWith2faDto;
import com.syberry.bakery.dto.ResetPasswordDto;
import com.syberry.bakery.dto.UpdatePasswordDto;
import com.syberry.bakery.dto.UserDto;
import com.syberry.bakery.service.AuthService;
import com.syberry.bakery.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("User signing in");
        AuthResponse authResponse = authService.login(loginRequestDto);
        if (authResponse instanceof LoginWith2faDto) {
            return ResponseEntity.ok(authResponse);
        }
        assert authResponse instanceof LoginDto;
        LoginDto loginDto = (LoginDto) authResponse;
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, loginDto.getCookie())
                .header(HttpHeaders.SET_COOKIE, loginDto.getRefreshCookie())
                .body(loginDto.getUserDto());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        log.info("Signing out");
        LoginDto loginDto = authService.logout();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, loginDto.getCookie())
                .header(HttpHeaders.SET_COOKIE, loginDto.getRefreshCookie())
                .build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        log.info("Processing refreshing token");
        LoginDto tokens = authService.refreshToken(request);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, tokens.getCookie())
                .header(HttpHeaders.SET_COOKIE, tokens.getRefreshCookie())
                .build();
    }

    @PostMapping("/email-verification")
    public ResponseEntity<UserDto> verify2FA(
            @Valid @RequestBody EmailVerificationDto emailVerificationDto) throws ExecutionException {
        log.info("Processing email verification");
        LoginDto loginDto = authService.verifyEmailCode(emailVerificationDto);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, loginDto.getCookie())
                .header(HttpHeaders.SET_COOKIE, loginDto.getRefreshCookie())
                .body(loginDto.getUserDto());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processResetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        log.info("Processing reset password");
        authService.resetPassword(resetPasswordDto);
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestResetPasswordEmail(@RequestParam String email) {
        log.info("Sending resetting password");
        userService.requestPasswordResettingEmail(email);
    }

    @PutMapping("/2fa/{status}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trigger2fa(@PathVariable String status) {
        log.info("Processing 2fa triggering (enabling/disabling)");
        authService.set2faStatus(status);
    }

    @PutMapping("/update-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(@Valid @RequestBody UpdatePasswordDto updatePasswordDto) {
        log.info("Processing updating password");
        authService.updatePassword(updatePasswordDto);
    }
}
