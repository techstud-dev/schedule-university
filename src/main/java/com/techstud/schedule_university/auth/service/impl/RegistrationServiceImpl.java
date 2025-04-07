package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.config.TokenProperties;
import com.techstud.schedule_university.auth.dto.request.RegisterDTO;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationDTO;
import com.techstud.schedule_university.auth.entity.PendingRegistration;
import com.techstud.schedule_university.auth.entity.RefreshToken;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.exception.InvalidCodeException;
import com.techstud.schedule_university.auth.exception.UserExistsException;
import com.techstud.schedule_university.auth.repository.UserRepository;
import com.techstud.schedule_university.auth.security.TokenService;
import com.techstud.schedule_university.auth.service.EmailConfirmationService;
import com.techstud.schedule_university.auth.service.RegistrationService;
import com.techstud.schedule_university.auth.service.UserCreationService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Сервис для управления процессом регистрации пользователей
 * <p>
 * Обрабатывает двухэтапную регистрацию:
 * 1. Инициирование регистрации с проверкой уникальности данных
 * 2. Подтверждение регистрации с валидацией кода
 * </p>
 */
@Service
public class RegistrationServiceImpl implements RegistrationService {
    @Qualifier("BeanTokenPropertiesBug")
    private final TokenProperties properties;
    private final EmailConfirmationService emailConfirmationService;
    private final UserCreationService userCreationService;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public RegistrationServiceImpl(@Qualifier("BeanTokenPropertiesBug") TokenProperties properties,
                                   EmailConfirmationService emailConfirmationService,
                                   UserCreationService userCreationService,
                                   UserRepository userRepository, TokenService tokenService) {
        this.properties = properties;
        this.emailConfirmationService = emailConfirmationService;
        this.userCreationService = userCreationService;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    /**
     * Начинает процесс регистрации нового пользователя
     *
     * @param dto DTO с данными для регистрации
     * @throws UserExistsException если пользователь с такими данными уже существует
     * @throws MessagingException при ошибке отправки email с кодом подтверждения
     */
    // В МИКРОСЕРВИСАХ ТУТ БУДЕТ КАФКА К СЕРВИСУ НОТИФИКАЦИЙ
    @Override
    @Transactional
    public void startRegistration(RegisterDTO dto) throws MessagingException, UserExistsException {
        if (userRepository.existsByUniqueFields(dto.username(), dto.email(), dto.phoneNumber())) {
            throw new UserExistsException();
        }
        emailConfirmationService.initiateConfirmation(dto);
    }

    /**
     * Завершает процесс регистрации
     *
     * @param code Код подтверждения из email
     * @return DTO с JWT токенами
     * @throws InvalidCodeException при неверном или просроченном коде
     * @throws UserExistsException при конфликте данных пользователя
     */
    // В МИКРОСЕРВИСАХ ТУТ БУДЕТ КАФКА К СЕРВИСУ НОТИФИКАЦИЙ
    @Override
    @Transactional
    public SuccessAuthenticationDTO completeRegistration(String code)
            throws InvalidCodeException, UserExistsException {
        PendingRegistration pending = emailConfirmationService.validateCode(code);
        User user = userCreationService.createPendingUser(pending);
        SuccessAuthenticationDTO successAuth = tokenService.generateTokens(user);
        updateUserRefreshToken(user, successAuth.refreshToken());
        return new SuccessAuthenticationDTO(successAuth.token(), successAuth.refreshToken());
    }

    /**
     * Обновляет refresh token пользователя
     *
     * @param user Пользователь для обновления
     * @param refreshToken Новый refresh token
     */
    private void updateUserRefreshToken(User user, String refreshToken) {
        user.setRefreshToken(new RefreshToken(
                refreshToken,
                Instant.now().plus(properties.getRefreshTokenExpiration(), ChronoUnit.SECONDS)
        ));
        userRepository.save(user);
    }
}
