package com.techstud.schedule_university.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterDTO(String requestId,
                          @NotBlank(message = "Username cannot be blank.")
                          String username,
                          @NotBlank(message = "Full name cannot be null or empty.")
                          String fullName,
                          @NotBlank(message = "Password cannot be blank.")
                          @Size(min = 8, message = "Password must be at least 8 characters long.")
                          String password,
                          @NotBlank(message = "Email cannot be blank.")
                          @Email(message = "Email format is invalid.")
                          String email,
                          @NotBlank(message = "Phone number cannot be blank.")
                          String phoneNumber,
                          @NotBlank(message = "Group number cannot be blank.")
                          String groupNumber,
                          @NotBlank(message = "University cannot be blank.")
                          String university) {

    public RegisterDTO(String username, String fullName, String password,
                       String email, String phoneNumber, String groupNumber,
                       String university) {
        this(UUID.randomUUID().toString(), username, fullName,
                password, email, phoneNumber, groupNumber, university);
    }
}
