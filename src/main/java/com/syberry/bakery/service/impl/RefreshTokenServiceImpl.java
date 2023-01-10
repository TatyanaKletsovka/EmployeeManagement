package com.syberry.bakery.service.impl;

import com.syberry.bakery.dto.LoginDto;
import com.syberry.bakery.entity.RefreshToken;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.exception.EntityNotFoundException;
import com.syberry.bakery.exception.TokenRefreshException;
import com.syberry.bakery.repository.RefreshTokenRepository;
import com.syberry.bakery.repository.UserRepository;
import com.syberry.bakery.service.RefreshTokenService;
import com.syberry.bakery.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    @Value("${bakery.security.jwtRefreshExpirationHr}")
    private Long refreshTokenDurationHr;

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;


    @Override
    public LoginDto refreshAccessToken(String token) {
        User user = verifyExpiration(token).getUser();
        ResponseCookie accessToken = jwtUtils.generateJwtCookie(user);
        ResponseCookie refreshToken = jwtUtils.generateRefreshJwtCookie(createRefreshToken(user.getId()).getToken());
        return new LoginDto(accessToken.toString(), refreshToken.toString(), null);
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findByIdAndIsBlockedFalse(userId)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
        RefreshToken refreshToken;
        if (refreshTokenRepository.findByUser(user).isPresent()) {
            refreshToken = refreshTokenRepository.findByUser(user)
                    .orElseThrow(() -> new EntityNotFoundException("Refresh token is not found"));
        } else {
            refreshToken = new RefreshToken();
            refreshToken.setUser(user);
        }
        refreshToken.setExpiryDate(Instant.now().plus(refreshTokenDurationHr, ChronoUnit.HOURS));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    @Override
    public RefreshToken verifyExpiration(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException("Token is not found"));
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token was expired. Please make a new sign in request");
        }
        return refreshToken;
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userRepository.findByIdAndIsBlockedFalse(userId).get().getId());
    }
}
