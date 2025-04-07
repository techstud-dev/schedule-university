package com.techstud.schedule_university.auth;

import com.techstud.schedule_university.auth.entity.University;
import com.techstud.schedule_university.auth.repository.UniversityRepository;
import com.techstud.schedule_university.auth.service.impl.UniversityServiceImpl;
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
public class UniversityServiceImplTest {

    @Mock
    private UniversityRepository repository;

    @InjectMocks
    private UniversityServiceImpl service;

    @Test
    void resolveUniversity_WhenUniversityExists_ShouldReturnExistingUniversity() {
        // Arrange
        String name = "BMSTU";
        University university = new University(name);

        // Act
        when(repository.findByName(name)).thenReturn(Optional.of(university));
        University result = service.resolveUniversity(name);

        // Assert
        assertEquals(name, result.getName());
        verify(repository, times(1)).findByName(name);
        verify(repository, never()).save(any());
    }

    @Test
    void resolveUniversity_WhenUniversityNotExists_ShouldReturnAndSaveNewUniversity() {
        // Arrange
        String name = "BMSTU";
        University university = new University(name);

        // Act
        when(repository.findByName(name)).thenReturn(Optional.empty());
        when(repository.save(any(University.class))).thenReturn(university);
        University result = service.resolveUniversity(name);

        // Assert
        ArgumentCaptor<University> captor = ArgumentCaptor.forClass(University.class);
        verify(repository).save(captor.capture());

        assertEquals(name, result.getName());
        assertEquals(name, captor.getValue().getName());
        verify(repository, times(1)).findByName(name);
    }
}
