package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.exception.*;
import com.joaoguilherme.tutoringmanagementsystem.model.*;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.BondStatus;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.EnrollmentStatus;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.NotificationEventType;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.SessionStatus;
import com.joaoguilherme.tutoringmanagementsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final NotificationRepository notificationRepository;

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
        Session session = sessionRepository.findById(sessionUuid).orElseThrow(() -> new SessionNotFoundException("Session not found"));

        if (!(isEvaluator(session.getBond(), userUuid))) {
            throw new UserWithoutPermissionException("User without permission");
        }

        if (session.getStatus() != SessionStatus.ACTIVE) {
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

    public Enrollment enroll(UUID sessionUuid, UUID studentUuid) {
        Session session = sessionRepository.findById(sessionUuid).orElseThrow(() -> new SessionNotFoundException("Session not found"));

        User student = userRepository.findById(studentUuid).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new InvalidSessionStatusException("Invalid session status");
        }

        boolean isAlreadyEnrolled = enrollmentRepository.existsBySessionAndStudentAndStatusIn(session, student, List.of(EnrollmentStatus.CONFIRMED, EnrollmentStatus.WAITLISTED));

        if (isAlreadyEnrolled) {
            throw new EnrollmentAlreadyExistsException("Enrollment already exists");
        }

        long confirmedCount = enrollmentRepository.countBySessionAndStatus(session, EnrollmentStatus.CONFIRMED);

        if (confirmedCount < session.getSpots()) {
            Enrollment enrollment = new Enrollment(EnrollmentStatus.CONFIRMED, session, student);
            return enrollmentRepository.save(enrollment);
        }

        if (session.isAllowsWaitlist()) {
            long waitlistedCount = enrollmentRepository.countBySessionAndStatus(session, EnrollmentStatus.WAITLISTED);

            int queuePosition = (int) waitlistedCount + 1;

            Enrollment enrollment = new Enrollment(EnrollmentStatus.WAITLISTED, session, student);

            enrollment.setQueuePosition(queuePosition);

            return enrollmentRepository.save(enrollment);
        }

        throw new SessionFullException("Session is full");
    }

    @Transactional
    public Enrollment cancelEnrollment(UUID enrollmentUuid, UUID userUuid) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentUuid).orElseThrow(() -> new EnrollmentNotFoundException("Enrollment not found"));

        if (!(enrollment.getStudent().getId().equals(userUuid))) {
            throw new UserWithoutPermissionException("User without permission");
        }

        if (enrollment.getStatus() != EnrollmentStatus.CONFIRMED && enrollment.getStatus() != EnrollmentStatus.WAITLISTED) {
            throw new InvalidEnrollmentStatusException("Invalid enrollment status");
        }

        EnrollmentStatus previousStatus = enrollment.getStatus();

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollment.setQueuePosition(null);

        List<Enrollment> waitlist = enrollmentRepository.findBySessionAndStatusOrderByQueuePositionAsc(enrollment.getSession(), EnrollmentStatus.WAITLISTED);

        if (previousStatus == EnrollmentStatus.CONFIRMED && !waitlist.isEmpty()) {
            Enrollment promoted = waitlist.get(0);
            promoted.setStatus(EnrollmentStatus.CONFIRMED);
            promoted.setQueuePosition(null);
            Notification notification = new Notification(NotificationEventType.SPOT_PROMOTED, "You have been promoted from the waitlist", promoted.getStudent());
            notificationRepository.save(notification);
        }

        int position = 1;

        for (Enrollment waiting : waitlist) {
            if (waiting.getStatus() == EnrollmentStatus.WAITLISTED) {
                waiting.setQueuePosition(position);
                position++;
            }
        }

        enrollmentRepository.saveAll(waitlist);
        return enrollmentRepository.save(enrollment);
    }

    private boolean isRequesterOrEvaluator(TutoringBond tutoringBond, UUID userId) {
        return (tutoringBond.getRequester().getId().equals(userId)) || (tutoringBond.getEvaluator() != null && tutoringBond.getEvaluator().getId().equals(userId));
    }

    private boolean isEvaluator(TutoringBond tutoringBond, UUID userId) {
        return (tutoringBond.getEvaluator() != null && tutoringBond.getEvaluator().getId().equals(userId));
    }
}
