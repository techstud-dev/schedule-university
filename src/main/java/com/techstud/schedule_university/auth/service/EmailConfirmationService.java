package com.techstud.schedule_university.auth.service;

import com.techstud.schedule_university.auth.dto.request.RegistrationRecord;
import com.techstud.schedule_university.auth.entity.PendingRegistration;
import com.techstud.schedule_university.auth.exception.InvalidCodeException;
import jakarta.mail.MessagingException;

public interface EmailConfirmationService {
    String initiateConfirmation(RegistrationRecord dto) throws MessagingException;
    PendingRegistration validateCode(String code) throws InvalidCodeException;
}
