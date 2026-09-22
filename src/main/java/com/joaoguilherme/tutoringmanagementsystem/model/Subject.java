package com.joaoguilherme.tutoringmanagementsystem.model;

import com.joaoguilherme.tutoringmanagementsystem.model.enums.SubjectStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subject")
@Getter
@Setter
@NoArgsConstructor
public class Subject {



    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(length = 255, unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private SubjectStatus status = SubjectStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;


    @ManyToOne
    @JoinColumn(name = "evaluator_id")
    private User evaluator;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Subject(User creator, String name) {
        this.creator = creator;
        this.name = name;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

}
