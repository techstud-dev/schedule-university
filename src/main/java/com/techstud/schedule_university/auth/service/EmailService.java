package com.techstud.schedule_university.auth.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendCode(String email, String code) throws MessagingException;
}
