package com.joaoguilherme.tutoringmanagementsystem.repository;

import com.joaoguilherme.tutoringmanagementsystem.model.Session;
import com.joaoguilherme.tutoringmanagementsystem.model.TutoringBond;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByBondAndStatusAndStartTimeAfter(TutoringBond tutoringBond, SessionStatus sessionStatus, OffsetDateTime offsetDateTime);
}
