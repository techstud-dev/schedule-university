package com.techstud.schedule_university.auth.service;

import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.exception.BadCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserFactory {

    private final BCryptPasswordEncoder passwordEncoder;

    public User createUser(String username, String fullName, String password, String email,
                           String phoneNumber, String groupNumber) throws Exception {
        if (username == null || fullName == null || password == null || email == null
                || phoneNumber == null || groupNumber == null) {
            throw new BadCredentialsException();
        }
        return User.builder()
                .username(username)
                .fullName(fullName)
                .password(encryptPassword(password))
                .email(email)
                .phoneNumber(phoneNumber)
                .groupNumber(groupNumber)
                .build();
    }

    private String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
