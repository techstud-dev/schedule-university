package com.techstud.schedule_university.auth;

import com.techstud.schedule_university.auth.entity.Role;
import com.techstud.schedule_university.auth.repository.RoleRepository;
import com.techstud.schedule_university.auth.service.RoleService;
import com.techstud.schedule_university.auth.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    @Mock
    private RoleRepository repository;

    @InjectMocks
    private RoleServiceImpl service;

    @Test
    void resolveRole_WhenRoleExists_ShouldReturnExistingRole() {
        // Arrange
        String name = "USER";
        Role someRole = new Role(name);

        // Act
        when(repository.findByName(name)).thenReturn(Optional.of(someRole));
        Role result = service.resolveRole(name);

        // Assert
        assertEquals(name, result.getName());
        verify(repository, times(1)).findByName(name);
        verify(repository, never()).save(any());
    }

    @Test
    void resolveRole_WhenRoleNotExists_ShouldCreateAndSaveNewRole() {
        // Arrange
        String name = "USER";
        Role savedRole = new Role(name);

        // Act
        when(repository.findByName(name)).thenReturn(Optional.empty());
        when(repository.save(any(Role.class))).thenReturn(savedRole);

        Role result = service.resolveRole(name);

        // Assert
        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(repository).save(captor.capture());

        assertEquals(name, captor.getValue().getName());
        assertEquals(name, result.getName());
        verify(repository, times(1)).findByName(name);
    }
}
