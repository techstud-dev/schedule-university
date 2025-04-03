package com.techstud.schedule_university.auth.repository;

import com.techstud.schedule_university.auth.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<University, Integer> {

    Optional<University> findByName(String universityName);
}
