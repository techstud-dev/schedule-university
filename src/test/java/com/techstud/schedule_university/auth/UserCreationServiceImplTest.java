package com.techstud.schedule_university.auth;

import com.techstud.schedule_university.auth.entity.PendingRegistration;
import com.techstud.schedule_university.auth.entity.Role;
import com.techstud.schedule_university.auth.entity.University;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.exception.UserExistsException;
import com.techstud.schedule_university.auth.repository.UserRepository;
import com.techstud.schedule_university.auth.service.RoleService;
import com.techstud.schedule_university.auth.service.UniversityService;
import com.techstud.schedule_university.auth.service.impl.UserCreationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserCreationServiceImplTest {

    @Mock
    private RoleService roleService;

    @Mock
    private UniversityService universityService;

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserCreationServiceImpl service;

    @Test
    void createPendingUser_WhenUserExists_ThrowsException() {
        // Arrange
        PendingRegistration pr = createTestPendingRegistration();
        when(repository.existsByUniqueFields(
                pr.getUsername(),
                pr.getEmail(),
                pr.getPhoneNumber()
        )).thenReturn(true);

        // Act & Assert
        assertThrows(UserExistsException.class, () ->
                service.createPendingUser(pr)
        );

        verify(repository).existsByUniqueFields(
                pr.getUsername(),
                pr.getEmail(),
                pr.getPhoneNumber()
        );
    }

    @Test
    void createPendingUser_WhenNewUser_ReturnsCreatedUser() throws UserExistsException {
        // Arrange
        PendingRegistration pr = createTestPendingRegistration();
        University mockUniversity = new University(pr.getUniversity());
        Role userRole = new Role("USER");
        String encodedPassword = "encoded_password";

        when(repository.existsByUniqueFields(any(), any(), any())).thenReturn(false);
        when(universityService.resolveUniversity(pr.getUniversity())).thenReturn(mockUniversity);
        when(roleService.resolveRole("USER")).thenReturn(userRole);
        when(encoder.encode(pr.getPassword())).thenReturn(encodedPassword);

        // Act
        User result = service.createPendingUser(pr);

        // Assert
        assertAll(
                () -> assertEquals(pr.getUsername(), result.getUsername()),
                () -> assertEquals(pr.getFullName(), result.getFullName()),
                () -> assertEquals(encodedPassword, result.getPassword()),
                () -> assertEquals(pr.getEmail(), result.getEmail()),
                () -> assertEquals(pr.getPhoneNumber(), result.getPhoneNumber()),
                () -> assertEquals(pr.getGroupNumber(), result.getGroupNumber()),
                () -> assertEquals(mockUniversity, result.getUniversity()),
                () -> assertTrue(result.getRoles().contains(userRole))
        );

        verify(encoder).encode(pr.getPassword());
        verify(roleService).resolveRole("USER");
        verify(universityService).resolveUniversity(pr.getUniversity());
    }

    private PendingRegistration createTestPendingRegistration() {
        return PendingRegistration.builder()
                .username("test_user")
                .fullName("Test User")
                .password("raw_password")
                .email("test@example.com")
                .phoneNumber("+123456789")
                .groupNumber("A-123")
                .university("Test University")
                .build();
    }
}
