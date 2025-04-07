package com.techstud.schedule_university.auth.service;

import com.techstud.schedule_university.auth.dto.request.RegisterDTO;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationDTO;
import com.techstud.schedule_university.auth.exception.InvalidCodeException;
import com.techstud.schedule_university.auth.exception.UserExistsException;
import jakarta.mail.MessagingException;

public interface RegistrationService {
    void startRegistration(RegisterDTO dto) throws MessagingException, UserExistsException;
    SuccessAuthenticationDTO completeRegistration(String code) throws InvalidCodeException, UserExistsException;
}
