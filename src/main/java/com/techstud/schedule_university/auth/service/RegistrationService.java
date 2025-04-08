package com.techstud.schedule_university.auth.service;

import com.techstud.schedule_university.auth.dto.request.RegistrationRecord;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationRecord;
import com.techstud.schedule_university.auth.exception.InvalidCodeException;
import com.techstud.schedule_university.auth.exception.UserExistsException;
import jakarta.mail.MessagingException;

public interface RegistrationService {
    void startRegistration(RegistrationRecord dto) throws MessagingException, UserExistsException;
    SuccessAuthenticationRecord completeRegistration(String code) throws InvalidCodeException, UserExistsException;
}
