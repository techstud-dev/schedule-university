package com.techstud.schedule_university.auth.repository;

import com.techstud.schedule_university.auth.entity.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {

    Optional<PendingRegistration> findByEmail(String email);

    Optional<PendingRegistration> findByConfirmationCode(String code);

    void deleteByExpirationDateBefore(Instant date);
}
