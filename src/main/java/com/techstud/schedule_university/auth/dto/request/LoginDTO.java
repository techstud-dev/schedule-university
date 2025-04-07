package com.techstud.schedule_university.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "Identification field cannot be blank.")
        String identificationField,

        @NotBlank(message = "Password cannot be blank.")
        String password
) {
}
