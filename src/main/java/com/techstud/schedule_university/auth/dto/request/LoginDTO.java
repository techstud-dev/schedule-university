package com.techstud.schedule_university.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record LoginDTO(String requestId,
                       @NotBlank(message = "Identification field cannot be blank.")
                       String identificationField,
                       @NotBlank(message = "Password cannot be blank.")
                       String password) {

    public LoginDTO(String identificationField, String password) {
        this(UUID.randomUUID().toString(), identificationField, password);
    }
}
