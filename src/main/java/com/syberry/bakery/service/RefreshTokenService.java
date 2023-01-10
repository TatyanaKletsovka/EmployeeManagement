package com.syberry.bakery.service;

import com.syberry.bakery.dto.LoginDto;
import com.syberry.bakery.entity.RefreshToken;
import org.springframework.http.ResponseCookie;

public interface RefreshTokenService {

    LoginDto refreshAccessToken(String accessToken);

    RefreshToken createRefreshToken(Long userId);

    RefreshToken verifyExpiration(String token);

    void deleteByUserId(Long userId);
}
