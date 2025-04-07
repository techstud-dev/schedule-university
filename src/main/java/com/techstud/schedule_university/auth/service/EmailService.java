package com.techstud.schedule_university.auth.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendCodeAsync(String email, String code) throws MessagingException;
}
