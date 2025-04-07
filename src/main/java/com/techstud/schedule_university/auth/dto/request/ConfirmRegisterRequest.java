package com.techstud.schedule_university.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmRegisterRequest(
        @NotBlank(message = "Invalid request.")
        String code
) {
}
