package com.joaoguilherme.tutoringmanagementsystem.model;

import com.joaoguilherme.tutoringmanagementsystem.model.enums.BondStatus;
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
@Table (name = "tutoring_bond")
@Getter
@Setter
@NoArgsConstructor
public class TutoringBond {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private BondStatus status = BondStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;


    @ManyToOne
    @JoinColumn(name = "evaluator_id")
    private User evaluator;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public TutoringBond(User requester, Subject subject) {
        this.requester = requester;
        this.subject = subject;
    }
}
