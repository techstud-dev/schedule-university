package com.techstud.schedule_university.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(String requestId,
                                  @NotBlank(message = "Refresh token cannot be blank.")
                                  String refreshToken) {
}
