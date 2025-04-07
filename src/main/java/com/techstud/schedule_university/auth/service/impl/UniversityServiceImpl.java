package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.entity.University;
import com.techstud.schedule_university.auth.repository.UniversityRepository;
import com.techstud.schedule_university.auth.service.UniversityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис управления университетами
 *
 * <p>Обеспечивает создание и поиск университетов по требованию</p>
 */
@Service
public class UniversityServiceImpl implements UniversityService {
    private final UniversityRepository repository;

    public UniversityServiceImpl(UniversityRepository repository) {
        this.repository = repository;
    }

    /**
     * Находит или создает новый университет
     *
     * @param name Название университета
     * @return Существующий или новый университет
     */
    @Override
    @Transactional
    public University resolveUniversity(String name) {
        return repository.findByName(name)
                .orElseGet(() -> createNewUniversity(name));
    }

    private University createNewUniversity(String name) {
        University university = new University(name);
        return repository.save(university);
    }
}
