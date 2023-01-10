package com.syberry.bakery.service.impl;

import com.google.common.cache.LoadingCache;
import com.syberry.bakery.converter.UserConverter;
import com.syberry.bakery.dto.AuthResponse;
import com.syberry.bakery.dto.EmailVerificationDto;
import com.syberry.bakery.dto.LoginDto;
import com.syberry.bakery.dto.LoginRequestDto;
import com.syberry.bakery.dto.LoginWith2faDto;
import com.syberry.bakery.dto.ResetPasswordDto;
import com.syberry.bakery.dto.Status;
import com.syberry.bakery.dto.UpdatePasswordDto;
import com.syberry.bakery.dto.UserDto;
import com.syberry.bakery.entity.RefreshToken;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.exception.EmailException;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.exception.InvalidArgumentTypeException;
import com.syberry.bakery.exception.TokenRefreshException;
import com.syberry.bakery.exception.UpdateException;
import com.syberry.bakery.repository.UserRepository;
import com.syberry.bakery.security.UserDetailsImpl;
import com.syberry.bakery.service.AuthService;
import com.syberry.bakery.service.EmailService;
import com.syberry.bakery.service.RefreshTokenService;
import com.syberry.bakery.service.UserService;
import com.syberry.bakery.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static com.syberry.bakery.util.SecurityContextUtil.getUserDetails;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final LoadingCache<String, String> oneTimePasswordCache;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder encoder;

    private final UserConverter userConverter;

    @Override
    public AuthResponse login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(),
                        loginRequestDto.getPassword()));
        User user = userRepository.findByEmailAndIsBlockedFalse(loginRequestDto.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
        if (user.is2faEnabled()) {
            sendEmailVerificationCode(loginRequestDto.getUsername());
            return new LoginWith2faDto("Verification code was sent to specified email");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return authenticateUser(userDetails);
    }

    @Override
    public LoginDto verifyEmailCode(EmailVerificationDto emailVerificationDto) {
        User user = userRepository.findByEmailAndIsBlockedFalse(emailVerificationDto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User with such email does not exists"));
        try {
            String code = oneTimePasswordCache.get(emailVerificationDto.getEmail());
            if (code.equals(emailVerificationDto.getCode().toString())) {
                oneTimePasswordCache.refresh(emailVerificationDto.getEmail());
                return authenticateUser(UserDetailsImpl.build(user));
            }
            throw new EmailException("Email verification code is invalid");
        } catch (ExecutionException e) {
            throw new EmailException("Email verification failed");
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT', 'USER')")
    public LoginDto refreshToken(HttpServletRequest request) {
        String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);
        if ((refreshToken != null) && (refreshToken.length() > 0)) {
            return refreshTokenService.refreshAccessToken(refreshToken);
        }
        throw new TokenRefreshException("Refresh token is empty");
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT', 'USER')")
    public LoginDto logout() {
        Long userId = getUserDetails().getId();
        refreshTokenService.deleteByUserId(userId);
        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie jwtRefreshCookie = jwtUtils.getCleanJwtRefreshCookie();

        return new LoginDto(jwtCookie.toString(), jwtRefreshCookie.toString(), null);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        try {
            String token = oneTimePasswordCache.get(resetPasswordDto.getEmail());
            if (token.equals(resetPasswordDto.getToken())) {
                oneTimePasswordCache.refresh(resetPasswordDto.getEmail());
                User user = userRepository.findByEmailAndIsBlockedFalse(resetPasswordDto.getEmail())
                        .orElseThrow(() -> new EntityNotFoundException("User is not found"));
                user.setPassword(encoder.encode(resetPasswordDto.getNewPassword()));
                return;
            }
            throw new EmailException("Email verification token is invalid");
        } catch (ExecutionException e) {
            throw new EmailException("Email verification failed");
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT', 'USER')")
    public void set2faStatus(String status) {
        try {
            UserDetailsImpl userDetails = getUserDetails();
            User user = userRepository.findByIdAndIsBlockedFalse(userDetails.getId())
                    .orElseThrow(() -> new EntityNotFoundException("User is not found"));
            user.set2faEnabled(Status.valueOf(status.toUpperCase()).getValue());
        } catch (IllegalArgumentException ex) {
            throw new InvalidArgumentTypeException("Invalid status type");
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'ACCOUNTANT', 'USER')")
    public void updatePassword(UpdatePasswordDto updatePasswordDto) {
        UserDetailsImpl userDetails = getUserDetails();
        User user = userRepository.findByIdAndIsBlockedFalse(userDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
        if (encoder.matches(updatePasswordDto.getCurrentPassword(), user.getPassword())) {
            user.setPassword(encoder.encode(updatePasswordDto.getNewPassword()));
            return;
        }
        throw new UpdateException("Invalid current password");
    }

    private void sendEmailVerificationCode(String userEmail) {
        String code = String.format("%06d", new Random().nextInt(999999));
        oneTimePasswordCache.put(userEmail, code);
        emailService.sendEmailVerificationCode(code, userEmail);
    }

    private LoginDto authenticateUser(UserDetailsImpl userDetails) {
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());
        return new LoginDto(jwtCookie.toString(), jwtRefreshCookie.toString(),
                getUserById(userDetails.getId()));
    }

    private UserDto getUserById(Long id) {
        return userConverter.convertToDto(userRepository.findByIdAndIsBlockedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User is not found")));
    }
}
