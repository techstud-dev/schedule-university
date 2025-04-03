package com.techstud.schedule_university.auth.dto.response;

import java.util.UUID;

public record SuccessAuthenticationDTO(String requestId,
                                       String token,
                                       String refreshToken) {

    public SuccessAuthenticationDTO(String token, String refreshToken) {
        this(UUID.randomUUID().toString(), token, refreshToken);
    }
}
