package com.techstud.schedule_university.auth.dto.response;

public record SuccessAuthenticationDTO(
        String token,
        String refreshToken
) {
}
