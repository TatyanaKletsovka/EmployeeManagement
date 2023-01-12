package com.syberry.bakery.service;

import com.google.common.cache.LoadingCache;
import com.syberry.bakery.converter.UserConverter;
import com.syberry.bakery.dto.LoginDto;
import com.syberry.bakery.dto.EmailVerificationDto;
import com.syberry.bakery.dto.LoginRequestDto;
import com.syberry.bakery.dto.LoginWith2faDto;
import com.syberry.bakery.dto.ResetPasswordDto;
import com.syberry.bakery.dto.RoleName;
import com.syberry.bakery.dto.UpdatePasswordDto;
import com.syberry.bakery.dto.UserDto;
import com.syberry.bakery.entity.RefreshToken;
import com.syberry.bakery.entity.Role;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.exception.EmailException;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.exception.InvalidArgumentTypeException;
import com.syberry.bakery.exception.TokenRefreshException;
import com.syberry.bakery.exception.UpdateException;
import com.syberry.bakery.repository.UserRepository;
import com.syberry.bakery.security.UserDetailsImpl;
import com.syberry.bakery.service.impl.AuthServiceImpl;
import com.syberry.bakery.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceUnitTest {
    @InjectMocks
    private AuthServiceImpl authService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private UserService userService;
    @Mock
    private LoadingCache<String, String> oneTimePasswordCache;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private Authentication authentication;
    @Mock
    private UserConverter userConverter;

    @Test
    void should_SuccessfullyLogin_with_2fa() {
        User user = new User();
        user.setId(1L);
        user.set2faEnabled(true);
        user.setIsBlocked(false);
        user.setEmail("test@mail.com");
        user.setRoles(new HashSet<>(List.of(new Role(1L, RoleName.ROLE_ADMIN))));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmailAndIsBlockedFalse(any())).thenReturn(Optional.of(user));
        when(emailService.getTemplate(any(), any())).thenReturn("body");

        assertThat(authService.login(new LoginRequestDto("test@mail.com", "test")))
                .isEqualTo(new LoginWith2faDto("Verification code was sent to specified email"));
    }

    @Test
    void should_ThrowError_When_SigningInWithNoneExistingEmail() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmailAndIsBlockedFalse(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> authService.login(new LoginRequestDto()));
    }


    @Nested
    class TestNest {
        @BeforeEach
        void mock_for_private_authenticate_method() {
            ResponseCookie jwtCookie = ResponseCookie.from("cookie", "cookie").build();
            when(jwtUtils.generateJwtCookie(any(UserDetailsImpl.class))).thenReturn(jwtCookie);
            when(refreshTokenService.createRefreshToken(any(Long.class))).thenReturn(new RefreshToken());
            ResponseCookie refreshJwtCookie = ResponseCookie
                    .from("refresh-token", "refresh-token").build();
            when(jwtUtils.generateRefreshJwtCookie(any())).thenReturn(refreshJwtCookie);
            UserDto userDto = new UserDto();
            userDto.setId(1L);
            userDto.setEmail("test@mail.com");
            when(userService.getUserById(any())).thenReturn(userDto);
        }

        @Test
        void should_SuccessfullyLogin_without_2fa() {
            ResponseCookie jwtCookie = ResponseCookie.from("cookie", "cookie").build();
            ResponseCookie refreshJwtCookie = ResponseCookie
                    .from("refresh-token", "refresh-token").build();
            User user = new User();
            user.setId(1L);
            user.set2faEnabled(false);
            user.setIsBlocked(false);
            user.setEmail("test@mail.com");
            user.setRoles(new HashSet<>(List.of(new Role(1L, RoleName.ROLE_ADMIN))));
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(userRepository.findByEmailAndIsBlockedFalse(any())).thenReturn(Optional.of(user));
            when(emailService.getTemplate(any(), any())).thenReturn("body");
            when(authentication.getPrincipal())
                    .thenReturn(new UserDetailsImpl(1L, null, "test@mail.com",  List.of()));
            UserDto userDto = new UserDto();
            userDto.setId(1L);
            userDto.setEmail("test@mail.com");
            when(userRepository.findByIdAndIsBlockedFalse(any())).thenReturn(Optional.of(user));
            when(userConverter.convertToDto(any(User.class))).thenReturn(userDto);
            assertThat(authService.login(new LoginRequestDto("test@mail.com", "test")))
                    .isEqualTo(new LoginDto(jwtCookie.toString(), refreshJwtCookie.toString(), userDto));
            verify(emailService, times(0)).sendEmail(any());
        }

        @Test
        void should_verifyEmailCode() throws ExecutionException {
            ResponseCookie jwtCookie = ResponseCookie.from("cookie", "cookie").build();
            ResponseCookie refreshJwtCookie = ResponseCookie.from("refresh-token", "refresh-token").build();
            User user = new User();
            user.setId(1L);
            user.setEmail("test@mail.com");
            when(userRepository.findByEmailAndIsBlockedFalse(any())).thenReturn(Optional.of(user));
            when(oneTimePasswordCache.get(any())).thenReturn("1234");
            when(userRepository.findByIdAndIsBlockedFalse(any())).thenReturn(Optional.of(user));
            UserDto userDto = new UserDto();
            userDto.setId(1L);
            userDto.setEmail(user.getEmail());
            when(userConverter.convertToDto(any(User.class))).thenReturn(userDto);
            LoginDto loginDto = authService.verifyEmailCode(new EmailVerificationDto("test@mail.com", 1234));
            LoginDto expectedLoginDto = new LoginDto(jwtCookie.toString(), refreshJwtCookie.toString(),
                    new UserDto(1L, null, null, "test@mail.com", null));
            assertThat(loginDto).isEqualTo(expectedLoginDto);
        }

        @Test
        void should_ThrowError_WhenVerifyingNoneExistingEmail() {
            when(userRepository.findByEmailAndIsBlockedFalse(any())).thenReturn(Optional.empty());
            assertThrows(EntityNotFoundException.class, () -> authService.verifyEmailCode(new EmailVerificationDto()));
        }

        @Test
        void should_ThrowError_WhenVerifyingExpiredCode() throws ExecutionException {
            when(userRepository.findByEmailAndIsBlockedFalse(any())).thenReturn(Optional.of(new User()));
            when(oneTimePasswordCache.get(any())).thenReturn("");
            assertThrows(EmailException.class, () -> authService
                    .verifyEmailCode(new EmailVerificationDto("test@mail.com", 1234)));
        }

        @Test
        void should_ThrowError_WhenVerifyingInvalidCode() throws ExecutionException {
            when(userRepository.findByEmailAndIsBlockedFalse(any())).thenReturn(Optional.of(new User()));
            when(oneTimePasswordCache.get(any())).thenReturn("1234");
            assertThrows(EmailException.class, () -> authService
                    .verifyEmailCode(new EmailVerificationDto("test@mail.com", 11111)));
        }
    }

    @Test
    void should_SuccessfullyRefreshToken() {
        String token = "refreshToken";
        when(jwtUtils.getJwtRefreshFromCookies(any())).thenReturn(token);
        ResponseCookie jwtCookie = ResponseCookie.from("cookie", token).build();
        LoginDto loginDto = new LoginDto(jwtCookie.toString(),"refresh",null);
        when(refreshTokenService.refreshAccessToken(any())).thenReturn(loginDto);
        assertThat(authService.refreshToken(null)).isEqualTo(loginDto);
    }

    @Test
    void should_ThrowError_When_RefreshingWithEmptyToken() {
        when(jwtUtils.getJwtRefreshFromCookies(any())).thenReturn("");
        assertThrows(TokenRefreshException.class, () -> authService.refreshToken(null));
    }

    @Test
    void should_ThrowError_When_RefreshingWithExpiredToken() {
        String token = "token";
        when(jwtUtils.getJwtRefreshFromCookies(any())).thenReturn(token);
        when(refreshTokenService.refreshAccessToken(any())).thenThrow(new TokenRefreshException("Refresh token was expired. Please make a new sign in request"));

        assertThrows(TokenRefreshException.class, () -> authService.refreshToken(null));

    }

    @Test
    void should_SuccessfullyLogout() {
        ResponseCookie jwtCookie = ResponseCookie.from("cookie", null).build();
        when(jwtUtils.getCleanJwtCookie()).thenReturn(jwtCookie);
        when(jwtUtils.getCleanJwtRefreshCookie()).thenReturn(jwtCookie);
        LoginDto expectedLoginDto = new LoginDto(jwtCookie.toString(), jwtCookie.toString(), null);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal())
                .thenReturn(new UserDetailsImpl(1L, null, "test@mail.com", null));

        assertThat(authService.logout()).isEqualTo(expectedLoginDto);
    }

    @Test
    void should_SuccessfullyProcessPasswordResetting() throws ExecutionException {
        String token = "token";
        String email = "test@mail.com";
        when(oneTimePasswordCache.get(any())).thenReturn(token);
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword("oldPassword");
        when(userRepository.findByEmailAndIsBlockedFalse(email)).thenReturn(Optional.of(user));
        when(encoder.encode(any())).thenReturn("newPassword");
        authService.resetPassword(new ResetPasswordDto(email, token, "password"));
        verify(userRepository, times(1)).findByEmailAndIsBlockedFalse(any());
        assertThat(user.getPassword()).isEqualTo("newPassword");
    }

    @Test
    void should_ThrowError_When_ProcessingPasswordResettingWithExpiredToken() throws ExecutionException {
        when(oneTimePasswordCache.get(any())).thenReturn("");
        assertThrows(EmailException.class, () -> authService
                .resetPassword(new ResetPasswordDto("test@mail.com", "token", "password")));
    }

    @Test
    void should_SuccessfullyDisable2fa() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal())
                .thenReturn(new UserDetailsImpl(1L, null, "test@mail.com",  null));
        User user = new User();
        user.set2faEnabled(true);
        when(userRepository.findByIdAndIsBlockedFalse(any())).thenReturn(Optional.of(user));
        authService.set2faStatus("disabled");
        assertThat(user.is2faEnabled()).isFalse();
    }

    @Test
    void should_ThrowError_When_Disabling2faWithInvalidStatusType() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal())
                .thenReturn(new UserDetailsImpl(1L, null, "test@mail.com", null));
        User user = new User();
        user.set2faEnabled(true);
        when(userRepository.findByIdAndIsBlockedFalse(any())).thenReturn(Optional.of(user));
        assertThrows(InvalidArgumentTypeException.class, () -> authService.set2faStatus("invalidType"));
    }

    @Test
    void should_SuccessfullyUpdatePassword() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal())
                .thenReturn(new UserDetailsImpl(1L, null, "test@mail.com",  null));
        User user = new User();
        user.setPassword("currentPassword");
        when(userRepository.findByIdAndIsBlockedFalse(any())).thenReturn(Optional.of(user));
        when(encoder.matches(any(), any())).thenReturn(true);
        when(encoder.encode(any())).thenReturn("newPassword");
        authService.updatePassword(new UpdatePasswordDto("currentPassword", "newPassword"));
        assertThat(user.getPassword()).isEqualTo("newPassword");
    }

    @Test
    void should_ThrowError_When_UpdatingPasswordWithInvalidCurrentPassword() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal())
                .thenReturn(new UserDetailsImpl(1L, null, "test@mail.com",  null));
        User user = new User();
        user.setPassword("currentPassword");
        when(userRepository.findByIdAndIsBlockedFalse(any())).thenReturn(Optional.of(user));
        when(encoder.matches(any(), any())).thenReturn(false);
        assertThrows(UpdateException.class,
                () -> authService.updatePassword(
                        new UpdatePasswordDto("currentPassword", "newPassword")));
    }
}
