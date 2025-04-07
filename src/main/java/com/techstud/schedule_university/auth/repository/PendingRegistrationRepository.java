package com.techstud.schedule_university.auth.repository;

import com.techstud.schedule_university.auth.entity.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {

    Optional<PendingRegistration> findByEmail(String email);

    Optional<PendingRegistration> findByConfirmationCode(String code);

    @Modifying
    @Query("""
        UPDATE PendingRegistration p\s
        SET p.confirmationCode = :code,
            p.expirationDate = :expDate,
            p.lastSentTime = :sentTime\s
        WHERE p.email = :email""")
    int updateExisting(
            @Param("email") String email,
            @Param("code") String code,
            @Param("expDate") Instant expDate,
            @Param("sentTime") Instant sentTime
    );

    @Modifying
    @Query(nativeQuery = true,
            value = """
           WITH to_delete AS (
               SELECT id\s
               FROM auth.pending_registration\s
               WHERE expiration_date < NOW()\s
               LIMIT :limit
           )
           DELETE FROM auth.pending_registration\s
           WHERE id IN (SELECT id FROM to_delete)
          \s""")
    int cleanupChunk(@Param("limit") int limit);

    @Modifying
    @Query("""
    UPDATE PendingRegistration p\s
    SET p.confirmationCode = :code,
        p.expirationDate = :expDate,
        p.lastSentTime = :sentTime\s
    WHERE p.id = :id""")
    void updateFields(
            @Param("id") Long id,
            @Param("code") String code,
            @Param("expDate") Instant expDate,
            @Param("sentTime") Instant sentTime);
}
