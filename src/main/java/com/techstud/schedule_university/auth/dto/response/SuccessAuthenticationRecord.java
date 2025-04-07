package com.techstud.schedule_university.auth.dto.response;

public record SuccessAuthenticationRecord(
        String token,
        String refreshToken
) {
}
