package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.exception.UserNotFoundException;
import com.techstud.schedule_university.auth.repository.UserRepository;
import com.techstud.schedule_university.auth.service.LogoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.techstud.schedule_university.auth.util.UtilConstants.NO_UPDATED_RECORDS;

/**
 * Сервис лог-аута пользователей
 *
 * <p>Обеспечивает механизм удаляет refresh token</p>
 */
@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutService {
    private final UserRepository repository;

    /**
     * Ищет юзера в бд по рефреш токену и удаляет его
     *
     * @param refreshToken действующий рефреш-токен
     * @throws UserNotFoundException выбрасывает если юзер не найден с таким рефреш-токеном
     */
    @Override
    public void logout(String refreshToken) throws UserNotFoundException {
        int updated = repository.clearRefreshToken(refreshToken);

        if (updated == NO_UPDATED_RECORDS) {
            throw new UserNotFoundException("No user found with refresh token") ;
        }
    }
}
