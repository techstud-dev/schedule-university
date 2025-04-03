package com.techstud.schedule_university.auth.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.exception.InvalidJwtTokenException;

public interface TokenService {
    String generateToken(User user);
    String generateRefreshToken(User user);
    String decodeIssuer(String token);
    DecodedJWT verifyToken(String token) throws InvalidJwtTokenException;
}
