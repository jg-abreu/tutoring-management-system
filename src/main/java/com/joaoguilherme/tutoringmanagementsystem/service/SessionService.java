package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.exception.*;
import com.joaoguilherme.tutoringmanagementsystem.model.Session;
import com.joaoguilherme.tutoringmanagementsystem.model.TutoringBond;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.BondStatus;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.SessionStatus;
import com.joaoguilherme.tutoringmanagementsystem.repository.EnrollmentRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.SessionRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.TutoringBondRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final TutoringBondRepository tutoringBondRepository;
    private final SessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public Session createSession(UUID tutoringBondUuid, UUID userId, OffsetDateTime startTime, OffsetDateTime endTime, int spots, boolean allowsWaitList) {

        if (!(endTime.isAfter(startTime))) {
            throw new InvalidTimeException("Invalid Time Exception");
        }

        TutoringBond tutoringBond = tutoringBondRepository.findById(tutoringBondUuid).orElseThrow(() -> new TutoringBondNotFoundException("TutoringBond not found"));

        if (!(isRequesterOrEvaluator(tutoringBond, userId))) {
            throw new UserWithoutPermissionException("User without permission");
        }

        if (tutoringBond.getStatus() != BondStatus.APPROVED) {
            throw new InvalidTutoringBondStatusException("Invalid tutoringBond status exception");
        }

        Session session = new Session(startTime, endTime, spots, allowsWaitList, tutoringBond);

        return sessionRepository.save(session);
    }

    public Session cancelSession(UUID sessionUuid, UUID userUuid) {
        Session session = sessionRepository.findById(sessionUuid).orElseThrow(() -> new SessionNotFoundException("Session not found exception"));

        if (!(isEvaluator(session.getBond(), userUuid))) {
            throw new UserWithoutPermissionException("User without permission");
        }

        if(session.getStatus() != SessionStatus.ACTIVE) {
            throw new InvalidSessionStatusException("Invalid Session Status Exception");
        }

        session.setStatus(SessionStatus.CANCELLED);
        return sessionRepository.save(session);

    }

    public List<Session> cancelFutureSessions(TutoringBond tutoringBond) {

        OffsetDateTime presentMoment = OffsetDateTime.now();

        List<Session> futureSessions = sessionRepository.findByBondAndStatusAndStartTimeAfter(tutoringBond, SessionStatus.ACTIVE, presentMoment);

        futureSessions.forEach(s -> s.setStatus(SessionStatus.CANCELLED));

        return sessionRepository.saveAll(futureSessions);
    }

    private boolean isRequesterOrEvaluator(TutoringBond tutoringBond, UUID userId) {
        return (tutoringBond.getRequester().getId().equals(userId)) || (tutoringBond.getEvaluator() != null && tutoringBond.getEvaluator().getId().equals(userId));
    }

    private boolean isEvaluator(TutoringBond tutoringBond, UUID userId) {
        return (tutoringBond.getEvaluator() != null && tutoringBond.getEvaluator().getId().equals(userId));
    }

}
