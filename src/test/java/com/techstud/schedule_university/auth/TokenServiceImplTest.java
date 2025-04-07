package com.techstud.schedule_university.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.techstud.schedule_university.auth.config.TokenProperties;
import com.techstud.schedule_university.auth.entity.Role;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.exception.InvalidJwtTokenException;
import com.techstud.schedule_university.auth.security.impl.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties(TokenProperties.class)
class TokenServiceImplTest {

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Mock
    private TokenProperties jwtProperties;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "SECRET_KEY", "mockSecretKey");
        ReflectionTestUtils.setField(tokenService, "authIssuer", "test-issuer");
        ReflectionTestUtils.setField(tokenService, "mainAudience", "test-audience");

        tokenService.initAlgorithms();
    }

    @Test
    void generateToken_ShouldReturnValidAccessToken() {
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(3600L); // 1 hour

        User user = User.builder()
                .username("testUser")
                .roles(Set.of(new Role("USER"), new Role("ADMIN")))
                .build();

        String token = tokenService.generateToken(user);

        assertNotNull(token);
        DecodedJWT decodedJWT = JWT.decode(token);

        assertEquals("test-issuer", decodedJWT.getIssuer());
        assertEquals("test-audience", decodedJWT.getAudience().get(0));
        assertEquals("testUser", decodedJWT.getSubject());
        assertEquals("access", decodedJWT.getClaim("type").asString());

        String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
        assertThat(roles).containsExactlyInAnyOrder("USER", "ADMIN");

        Instant expiration = decodedJWT.getExpiresAt().toInstant();
        assertTrue(expiration.isAfter(Instant.now().plusSeconds(3590))); // ~1 hour
        assertTrue(expiration.isBefore(Instant.now().plusSeconds(3610))); // ~1 hour

        verify(jwtProperties).getAccessTokenExpiration();
    }

    @Test
    void verifyToken_ShouldReturnDecodedJWT_WhenValidToken() throws InvalidJwtTokenException {
        User user = User.builder()
                .username("testUser")
                .roles(Set.of(new Role("USER")))
                .build();

        String validToken = tokenService.generateToken(user);
        String bearerToken = "Bearer " + validToken;

        DecodedJWT decodedJWT = tokenService.verifyToken(bearerToken);

        assertNotNull(decodedJWT);
        assertEquals("testUser", decodedJWT.getSubject());
    }

    @Test
    void verifyToken_ShouldThrowException_WhenInvalidToken() {
        String invalidToken = "Bearer invalid.token";

        assertThrows(InvalidJwtTokenException.class, () -> tokenService.verifyToken(invalidToken));
    }

    @Test
    void decodeIssuer_ShouldReturnIssuer_WhenValidToken() {
        User user = User.builder()
                .username("testUser")
                .roles(Set.of(new Role("USER")))
                .build();

        String validToken = tokenService.generateToken(user);
        String bearerToken = "Bearer " + validToken;

        String issuer = tokenService.decodeIssuer(bearerToken);

        assertEquals("test-issuer", issuer);
    }

    @Test
    void decodeIssuer_ShouldThrowException_WhenInvalidToken() {
        String invalidToken = "Bearer invalid.token";

        assertThrows(IllegalArgumentException.class, () -> tokenService.decodeIssuer(invalidToken));
    }

    @Test
    void initAlgorithms_ShouldThrowException_WhenSecretKeyIsNull() {
        ReflectionTestUtils.setField(tokenService, "SECRET_KEY", null);

        assertThrows(IllegalArgumentException.class, () -> tokenService.initAlgorithms());
    }

    @Test
    void initAlgorithms_ShouldThrowException_WhenSecretKeyIsEmpty() {
        ReflectionTestUtils.setField(tokenService, "SECRET_KEY", "");

        assertThrows(IllegalArgumentException.class, () -> tokenService.initAlgorithms());
    }
}
