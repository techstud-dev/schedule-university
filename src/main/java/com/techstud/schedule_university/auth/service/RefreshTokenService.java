package com.techstud.schedule_university.auth.service;

public interface RefreshTokenService {
    String refreshToken(String token) throws Exception;
}
