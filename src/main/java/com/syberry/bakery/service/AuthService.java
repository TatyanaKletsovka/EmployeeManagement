package com.syberry.bakery.service;

import com.syberry.bakery.dto.AuthResponse;
import com.syberry.bakery.dto.LoginDto;
import com.syberry.bakery.dto.EmailVerificationDto;
import com.syberry.bakery.dto.LoginRequestDto;
import com.syberry.bakery.dto.ResetPasswordDto;
import com.syberry.bakery.dto.UpdatePasswordDto;
import org.springframework.http.ResponseCookie;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

public interface AuthService {
    AuthResponse login(LoginRequestDto loginRequestDto);

    LoginDto refreshToken(HttpServletRequest request);

    LoginDto logout();

    void resetPassword(ResetPasswordDto resetPasswordDto);

    void updatePassword(UpdatePasswordDto updatePasswordDto);

    LoginDto verifyEmailCode(EmailVerificationDto emailVerificationDto) throws ExecutionException;

    void set2faStatus(String status);
}
