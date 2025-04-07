package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.entity.University;
import com.techstud.schedule_university.auth.repository.UniversityRepository;
import com.techstud.schedule_university.auth.service.UniversityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UniversityServiceImpl implements UniversityService {
    private final UniversityRepository repository;

    public UniversityServiceImpl(UniversityRepository repository) {
        this.repository = repository;
    }

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
