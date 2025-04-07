package com.techstud.schedule_university.auth.util;

import com.techstud.schedule_university.auth.config.JwtProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CookieUtil {

    @Qualifier("JwtProperties")
    private final JwtProperties jwtProperties;

    public CookieUtil(@Qualifier("JwtProperties") JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public ResponseCookie createHttpOnlyCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();
    }

    public List<ResponseCookie> createAuthCookies(String accessToken, String refreshToken) {
        return List.of(
                createHttpOnlyCookie("accessToken", accessToken, jwtProperties.getAccessTokenExpiration()),
                createHttpOnlyCookie("refreshToken", refreshToken, jwtProperties.getRefreshTokenExpiration())
        );
    }

    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return createHttpOnlyCookie("accessToken", accessToken, jwtProperties.getAccessTokenExpiration());
    }
}
