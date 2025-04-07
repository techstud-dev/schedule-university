package com.techstud.schedule_university.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.Instant;

@Data
@Entity
@Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "pending_registration", schema = "auth")
public class PendingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pending_id_seq")
    @SequenceGenerator(name = "pending_id_seq", schema = "auth", sequenceName = "pending_id_seq", allocationSize = 1)
    private Long id;

    private String username;

    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    private String password;
    private String groupNumber;
    private String university;
    private String confirmationCode;
    private Instant expirationDate;

    @Column(updatable = false)
    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp modifiedAt;
}
