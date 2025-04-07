package com.techstud.schedule_university.auth.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.exception.InvalidJwtTokenException;
import com.techstud.schedule_university.auth.repository.UserRepository;
import com.techstud.schedule_university.auth.security.TokenService;
import com.techstud.schedule_university.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для обновления JWT токенов
 *
 * <p>Обеспечивает механизм обновления access token с использованием refresh token</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    /**
     * Обновляет access token
     *
     * @param token Refresh token
     * @return Новый access token
     * @throws InvalidJwtTokenException Если:
     * - токен пустой
     * - токен невалиден
     * - пользователь не найден
     */
    @Override
    @Transactional(readOnly = true)
    public String refreshToken(String token) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new InvalidJwtTokenException();
        }

        DecodedJWT decodedJWT = tokenService.verifyToken(token);

        if (decodedJWT == null) {
            throw new InvalidJwtTokenException();
        }

        String username = decodedJWT.getSubject();
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new InvalidJwtTokenException("User not found for refresh token."));

        log.info("Refreshing token for user: {}", username);
        return tokenService.generateToken(user);
    }
}
