package com.techstud.schedule_university.auth.security.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.techstud.schedule_university.auth.config.JwtProperties;
import com.techstud.schedule_university.auth.entity.Role;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.exception.InvalidJwtTokenException;
import com.techstud.schedule_university.auth.security.TokenService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.auth-issuer}")
    private String authIssuer;

    @Value("${jwt.main-audience}")
    private String mainAudience;

    @Qualifier("JwtProperties")
    private JwtProperties properties;

    private Algorithm authAlgorithm;

    public TokenServiceImpl(@Qualifier("JwtProperties") JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initAlgorithms() {
        log.debug("Initializing algorithms...");
        if (SECRET_KEY == null || SECRET_KEY.isEmpty()) {
            throw new IllegalArgumentException("The Secret cannot be null or empty");
        }
        this.authAlgorithm = Algorithm.HMAC256(SECRET_KEY);
    }

    @Override
    public String generateToken(User user) {
        log.info("Generating access token for: {}", user.getUsername());
        return JWT.create()
                .withIssuer(authIssuer)
                .withAudience(mainAudience)
                .withSubject(user.getUsername())
                .withClaim("type", "access")
                .withArrayClaim("roles", user.getRoles().stream()
                        .map(Role::getAuthority).toArray(String[]::new))
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plus(properties.getAccessTokenExpiration(),
                        ChronoUnit.SECONDS)))
                .sign(authAlgorithm);
    }

    @Override
    public String generateRefreshToken(User user) {
        log.info("Generating refresh token for: {}", user.getUsername());
        return JWT.create()
                .withIssuer(authIssuer)
                .withAudience(mainAudience)
                .withSubject(user.getUsername())
                .withClaim("type", "refresh")
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plus(properties.getRefreshTokenExpiration(),
                        ChronoUnit.SECONDS)))
                .sign(authAlgorithm);
    }

    @Override
    public DecodedJWT verifyToken(String token) throws InvalidJwtTokenException {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring("Bearer ".length()).trim();
            }

            JWTVerifier verifier = JWT.require(authAlgorithm).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.error("Token verification failed: {}", e.getMessage());
            throw new InvalidJwtTokenException("Token verification failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String decodeIssuer(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring("Bearer ".length()).trim();
            }

            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getIssuer();
        } catch (JWTDecodeException e) {
            log.error("Failed to decode token issuer: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to decode token issuer: " + e.getMessage(), e);
        }
    }
}
