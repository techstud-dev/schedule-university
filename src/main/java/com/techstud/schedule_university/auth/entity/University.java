package com.techstud.schedule_university.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "universities", schema = "auth")
public class University {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "university_id_seq")
    @SequenceGenerator(name = "university_id_seq", schema = "auth", sequenceName = "university_id_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();

    public University(String university) {
        this.name = university;
    }
}
