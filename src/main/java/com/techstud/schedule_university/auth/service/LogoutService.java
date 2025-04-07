package com.techstud.schedule_university.auth.service;

import com.techstud.schedule_university.auth.exception.UserNotFoundException;

public interface LogoutService {
    void logout(String refreshToken) throws UserNotFoundException;
}
