package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.exception.EnrollmentAlreadyExistsException;
import com.joaoguilherme.tutoringmanagementsystem.exception.EnrollmentNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidEnrollmentStatusException;
import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidSessionStatusException;
import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidTimeException;
import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidTutoringBondStatusException;
import com.joaoguilherme.tutoringmanagementsystem.exception.SessionFullException;
import com.joaoguilherme.tutoringmanagementsystem.exception.SessionNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.exception.TutoringBondNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.exception.UserNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.exception.UserWithoutPermissionException;
import com.joaoguilherme.tutoringmanagementsystem.model.Enrollment;
import com.joaoguilherme.tutoringmanagementsystem.model.Notification;
import com.joaoguilherme.tutoringmanagementsystem.model.Session;
import com.joaoguilherme.tutoringmanagementsystem.model.Subject;
import com.joaoguilherme.tutoringmanagementsystem.model.TutoringBond;
import com.joaoguilherme.tutoringmanagementsystem.model.User;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.BondStatus;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.EnrollmentStatus;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.NotificationEventType;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.SessionStatus;
import com.joaoguilherme.tutoringmanagementsystem.repository.EnrollmentRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.NotificationRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.SessionRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.TutoringBondRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    private TutoringBondRepository tutoringBondRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private SessionService sessionService;


    @Test
    void shouldCreateSessionWhenUserIsRequester() {

        UUID tutoringBondUuid = UUID.randomUUID();

        User requester = new User();
        requester.setId(UUID.randomUUID());

        TutoringBond tutoringBond = new TutoringBond(requester, new Subject());
        tutoringBond.setId(tutoringBondUuid);
        tutoringBond.setStatus(BondStatus.APPROVED);

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endTime = startTime.plusHours(2);

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(sessionRepository.save(any(Session.class))).then(returnsFirstArg());

        Session sessionTest = sessionService.createSession(tutoringBondUuid, requester.getId(), startTime, endTime, 5, true);

        Assertions.assertEquals(startTime, sessionTest.getStartTime());
        Assertions.assertEquals(endTime, sessionTest.getEndTime());
        Assertions.assertEquals(5, sessionTest.getSpots());
        Assertions.assertTrue(sessionTest.isAllowsWaitlist());
        Assertions.assertEquals(tutoringBond, sessionTest.getBond());
        Assertions.assertEquals(SessionStatus.ACTIVE, sessionTest.getStatus());

        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void shouldCreateSessionWhenUserIsEvaluator() {

        UUID tutoringBondUuid = UUID.randomUUID();

        User requester = new User();
        requester.setId(UUID.randomUUID());

        User evaluator = new User();
        evaluator.setId(UUID.randomUUID());

        TutoringBond tutoringBond = new TutoringBond(requester, new Subject());
        tutoringBond.setId(tutoringBondUuid);
        tutoringBond.setStatus(BondStatus.APPROVED);
        tutoringBond.setEvaluator(evaluator);

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endTime = startTime.plusHours(2);

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(sessionRepository.save(any(Session.class))).then(returnsFirstArg());

        Session sessionTest = sessionService.createSession(tutoringBondUuid, evaluator.getId(), startTime, endTime, 5, false);

        Assertions.assertEquals(tutoringBond, sessionTest.getBond());

        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void shouldThrowInvalidTimeExceptionWhenEndTimeIsBeforeStartTime() {

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endTime = startTime.minusHours(1);

        Assertions.assertThrows(InvalidTimeException.class, () -> sessionService.createSession(UUID.randomUUID(), UUID.randomUUID(), startTime, endTime, 5, false));

        verifyNoInteractions(tutoringBondRepository, sessionRepository);
    }

    @Test
    void shouldThrowInvalidTimeExceptionWhenEndTimeEqualsStartTime() {

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);

        Assertions.assertThrows(InvalidTimeException.class, () -> sessionService.createSession(UUID.randomUUID(), UUID.randomUUID(), startTime, startTime, 5, false));

        verifyNoInteractions(tutoringBondRepository, sessionRepository);
    }

    @Test
    void shouldThrowTutoringBondNotFoundExceptionWhenBondDoesNotExist() {

        UUID tutoringBondUuid = UUID.randomUUID();

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endTime = startTime.plusHours(2);

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(TutoringBondNotFoundException.class, () -> sessionService.createSession(tutoringBondUuid, UUID.randomUUID(), startTime, endTime, 5, false));

        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void shouldThrowUserWithoutPermissionExceptionWhenUserIsNotRequesterNorEvaluator() {

        UUID tutoringBondUuid = UUID.randomUUID();

        User requester = new User();
        requester.setId(UUID.randomUUID());

        User evaluator = new User();
        evaluator.setId(UUID.randomUUID());

        TutoringBond tutoringBond = new TutoringBond(requester, new Subject());
        tutoringBond.setId(tutoringBondUuid);
        tutoringBond.setStatus(BondStatus.APPROVED);
        tutoringBond.setEvaluator(evaluator);

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endTime = startTime.plusHours(2);

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));

        Assertions.assertThrows(UserWithoutPermissionException.class, () -> sessionService.createSession(tutoringBondUuid, UUID.randomUUID(), startTime, endTime, 5, false));

        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void shouldThrowUserWithoutPermissionExceptionWhenBondIsPendingAndUserIsNotRequester() {

        UUID tutoringBondUuid = UUID.randomUUID();

        User requester = new User();
        requester.setId(UUID.randomUUID());

        TutoringBond tutoringBond = new TutoringBond(requester, new Subject());
        tutoringBond.setId(tutoringBondUuid);

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endTime = startTime.plusHours(2);

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));

        Assertions.assertThrows(UserWithoutPermissionException.class, () -> sessionService.createSession(tutoringBondUuid, UUID.randomUUID(), startTime, endTime, 5, false));

        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void shouldThrowInvalidTutoringBondStatusExceptionWhenBondIsNotApproved() {

        UUID tutoringBondUuid = UUID.randomUUID();

        User requester = new User();
        requester.setId(UUID.randomUUID());

        TutoringBond tutoringBond = new TutoringBond(requester, new Subject());
        tutoringBond.setId(tutoringBondUuid);

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endTime = startTime.plusHours(2);

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));

        Assertions.assertThrows(InvalidTutoringBondStatusException.class, () -> sessionService.createSession(tutoringBondUuid, requester.getId(), startTime, endTime, 5, false));

        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void shouldCancelSessionWhenUserIsEvaluatorAndSessionIsActive() {

        UUID sessionUuid = UUID.randomUUID();

        User requester = new User();
        requester.setId(UUID.randomUUID());

        User evaluator = new User();
        evaluator.setId(UUID.randomUUID());

        TutoringBond tutoringBond = new TutoringBond(requester, new Subject());
        tutoringBond.setStatus(BondStatus.APPROVED);
        tutoringBond.setEvaluator(evaluator);

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 5, false, tutoringBond);
        session.setId(sessionUuid);

        when(sessionRepository.findById(sessionUuid)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).then(returnsFirstArg());

        Session sessionTest = sessionService.cancelSession(sessionUuid, evaluator.getId());

        Assertions.assertEquals(SessionStatus.CANCELLED, sessionTest.getStatus());

        verify(sessionRepository).save(session);
    }

    @Test
    void shouldThrowSessionNotFoundExceptionWhenSessionDoesNotExist() {

        UUID sessionUuid = UUID.randomUUID();

        when(sessionRepository.findById(sessionUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(SessionNotFoundException.class, () -> sessionService.cancelSession(sessionUuid, UUID.randomUUID()));

        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void shouldThrowUserWithoutPermissionExceptionWhenUserIsRequester() {

        UUID sessionUuid = UUID.randomUUID();

        User requester = new User();
        requester.setId(UUID.randomUUID());

        User evaluator = new User();
        evaluator.setId(UUID.randomUUID());

        TutoringBond tutoringBond = new TutoringBond(requester, new Subject());
        tutoringBond.setStatus(BondStatus.APPROVED);
        tutoringBond.setEvaluator(evaluator);

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 5, false, tutoringBond);
        session.setId(sessionUuid);

        when(sessionRepository.findById(sessionUuid)).thenReturn(Optional.of(session));

        Assertions.assertThrows(UserWithoutPermissionException.class, () -> sessionService.cancelSession(sessionUuid, requester.getId()));

        Assertions.assertEquals(SessionStatus.ACTIVE, session.getStatus());

        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void shouldThrowUserWithoutPermissionExceptionWhenBondHasNoEvaluator() {

        UUID sessionUuid = UUID.randomUUID();

        User requester = new User();
        requester.setId(UUID.randomUUID());

        TutoringBond tutoringBond = new TutoringBond(requester, new Subject());

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 5, false, tutoringBond);
        session.setId(sessionUuid);

        when(sessionRepository.findById(sessionUuid)).thenReturn(Optional.of(session));

        Assertions.assertThrows(UserWithoutPermissionException.class, () -> sessionService.cancelSession(sessionUuid, UUID.randomUUID()));

        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void shouldThrowInvalidSessionStatusExceptionWhenSessionIsAlreadyCancelled() {

        UUID sessionUuid = UUID.randomUUID();

        User requester = new User();
        requester.setId(UUID.randomUUID());

        User evaluator = new User();
        evaluator.setId(UUID.randomUUID());

        TutoringBond tutoringBond = new TutoringBond(requester, new Subject());
        tutoringBond.setStatus(BondStatus.APPROVED);
        tutoringBond.setEvaluator(evaluator);

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 5, false, tutoringBond);
        session.setId(sessionUuid);
        session.setStatus(SessionStatus.CANCELLED);

        when(sessionRepository.findById(sessionUuid)).thenReturn(Optional.of(session));

        Assertions.assertThrows(InvalidSessionStatusException.class, () -> sessionService.cancelSession(sessionUuid, evaluator.getId()));

        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void shouldCancelFutureActiveSessionsWhenBondHasSessions() {

        TutoringBond tutoringBond = new TutoringBond(new User(), new Subject());
        tutoringBond.setStatus(BondStatus.APPROVED);

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session firstSession = new Session(startTime, startTime.plusHours(2), 5, false, tutoringBond);
        Session secondSession = new Session(startTime.plusDays(7), startTime.plusDays(7).plusHours(2), 5, false, tutoringBond);
        List<Session> futureSessions = List.of(firstSession, secondSession);

        when(sessionRepository.findByBondAndStatusAndStartTimeAfter(eq(tutoringBond), eq(SessionStatus.ACTIVE), any(OffsetDateTime.class))).thenReturn(futureSessions);
        when(sessionRepository.saveAll(futureSessions)).thenReturn(futureSessions);

        List<Session> cancelledSessions = sessionService.cancelFutureSessions(tutoringBond);

        Assertions.assertEquals(2, cancelledSessions.size());
        Assertions.assertEquals(SessionStatus.CANCELLED, firstSession.getStatus());
        Assertions.assertEquals(SessionStatus.CANCELLED, secondSession.getStatus());

        verify(sessionRepository).saveAll(futureSessions);
    }

    @Test
    void shouldQueryOnlyActiveSessionsWhenCancellingFutureSessions() {

        TutoringBond tutoringBond = new TutoringBond(new User(), new Subject());

        OffsetDateTime before = OffsetDateTime.now();

        sessionService.cancelFutureSessions(tutoringBond);

        OffsetDateTime after = OffsetDateTime.now();

        ArgumentCaptor<OffsetDateTime> referenceTime = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(sessionRepository).findByBondAndStatusAndStartTimeAfter(eq(tutoringBond), eq(SessionStatus.ACTIVE), referenceTime.capture());

        Assertions.assertFalse(referenceTime.getValue().isBefore(before));
        Assertions.assertFalse(referenceTime.getValue().isAfter(after));
    }

    @Test
    void shouldReturnEmptyListWhenBondHasNoFutureSessions() {

        TutoringBond tutoringBond = new TutoringBond(new User(), new Subject());

        when(sessionRepository.findByBondAndStatusAndStartTimeAfter(eq(tutoringBond), eq(SessionStatus.ACTIVE), any(OffsetDateTime.class))).thenReturn(List.of());
        when(sessionRepository.saveAll(List.<Session>of())).thenReturn(List.of());

        List<Session> cancelledSessions = sessionService.cancelFutureSessions(tutoringBond);

        Assertions.assertTrue(cancelledSessions.isEmpty());
    }

    @Test
    void shouldConfirmEnrollmentWhenThereAreAvailableSpots() {

        UUID sessionUuid = UUID.randomUUID();

        User student = new User();
        student.setId(UUID.randomUUID());

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 5, false, new TutoringBond(new User(), new Subject()));
        session.setId(sessionUuid);

        when(sessionRepository.findById(sessionUuid)).thenReturn(Optional.of(session));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsBySessionAndStudentAndStatusIn(session, student, List.of(EnrollmentStatus.CONFIRMED, EnrollmentStatus.WAITLISTED))).thenReturn(false);
        when(enrollmentRepository.countBySessionAndStatus(session, EnrollmentStatus.CONFIRMED)).thenReturn(4L);
        when(enrollmentRepository.save(any(Enrollment.class))).then(returnsFirstArg());

        Enrollment enrollmentTest = sessionService.enroll(sessionUuid, student.getId());

        Assertions.assertEquals(EnrollmentStatus.CONFIRMED, enrollmentTest.getStatus());
        Assertions.assertNull(enrollmentTest.getQueuePosition());
        Assertions.assertEquals(session, enrollmentTest.getSession());
        Assertions.assertEquals(student, enrollmentTest.getStudent());

        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void shouldAddToWaitlistWhenSessionIsFullAndAllowsWaitlist() {

        UUID sessionUuid = UUID.randomUUID();

        User student = new User();
        student.setId(UUID.randomUUID());

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 2, true, new TutoringBond(new User(), new Subject()));
        session.setId(sessionUuid);

        when(sessionRepository.findById(sessionUuid)).thenReturn(Optional.of(session));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsBySessionAndStudentAndStatusIn(session, student, List.of(EnrollmentStatus.CONFIRMED, EnrollmentStatus.WAITLISTED))).thenReturn(false);
        when(enrollmentRepository.countBySessionAndStatus(session, EnrollmentStatus.CONFIRMED)).thenReturn(2L);
        when(enrollmentRepository.countBySessionAndStatus(session, EnrollmentStatus.WAITLISTED)).thenReturn(2L);
        when(enrollmentRepository.save(any(Enrollment.class))).then(returnsFirstArg());

        Enrollment enrollmentTest = sessionService.enroll(sessionUuid, student.getId());

        Assertions.assertEquals(EnrollmentStatus.WAITLISTED, enrollmentTest.getStatus());
        Assertions.assertEquals(3, enrollmentTest.getQueuePosition());
        Assertions.assertEquals(session, enrollmentTest.getSession());
        Assertions.assertEquals(student, enrollmentTest.getStudent());

        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void shouldThrowSessionFullExceptionWhenSessionIsFullAndDoesNotAllowWaitlist() {

        UUID sessionUuid = UUID.randomUUID();

        User student = new User();
        student.setId(UUID.randomUUID());

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 2, false, new TutoringBond(new User(), new Subject()));
        session.setId(sessionUuid);

        when(sessionRepository.findById(sessionUuid)).thenReturn(Optional.of(session));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsBySessionAndStudentAndStatusIn(session, student, List.of(EnrollmentStatus.CONFIRMED, EnrollmentStatus.WAITLISTED))).thenReturn(false);
        when(enrollmentRepository.countBySessionAndStatus(session, EnrollmentStatus.CONFIRMED)).thenReturn(2L);

        Assertions.assertThrows(SessionFullException.class, () -> sessionService.enroll(sessionUuid, student.getId()));

        verify(enrollmentRepository, never()).countBySessionAndStatus(session, EnrollmentStatus.WAITLISTED);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void shouldThrowSessionNotFoundExceptionWhenEnrollingInNonexistentSession() {

        UUID sessionUuid = UUID.randomUUID();

        when(sessionRepository.findById(sessionUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(SessionNotFoundException.class, () -> sessionService.enroll(sessionUuid, UUID.randomUUID()));

        verifyNoInteractions(userRepository, enrollmentRepository);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenStudentDoesNotExist() {

        UUID sessionUuid = UUID.randomUUID();
        UUID studentUuid = UUID.randomUUID();

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 5, false, new TutoringBond(new User(), new Subject()));
        session.setId(sessionUuid);

        when(sessionRepository.findById(sessionUuid)).thenReturn(Optional.of(session));
        when(userRepository.findById(studentUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> sessionService.enroll(sessionUuid, studentUuid));

        verifyNoInteractions(enrollmentRepository);
    }

    @Test
    void shouldThrowInvalidSessionStatusExceptionWhenEnrollingInCancelledSession() {

        UUID sessionUuid = UUID.randomUUID();

        User student = new User();
        student.setId(UUID.randomUUID());

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 5, false, new TutoringBond(new User(), new Subject()));
        session.setId(sessionUuid);
        session.setStatus(SessionStatus.CANCELLED);

        when(sessionRepository.findById(sessionUuid)).thenReturn(Optional.of(session));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        Assertions.assertThrows(InvalidSessionStatusException.class, () -> sessionService.enroll(sessionUuid, student.getId()));

        verifyNoInteractions(enrollmentRepository);
    }

    @Test
    void shouldThrowEnrollmentAlreadyExistsExceptionWhenStudentIsAlreadyEnrolled() {

        UUID sessionUuid = UUID.randomUUID();

        User student = new User();
        student.setId(UUID.randomUUID());

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 5, false, new TutoringBond(new User(), new Subject()));
        session.setId(sessionUuid);

        when(sessionRepository.findById(sessionUuid)).thenReturn(Optional.of(session));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsBySessionAndStudentAndStatusIn(session, student, List.of(EnrollmentStatus.CONFIRMED, EnrollmentStatus.WAITLISTED))).thenReturn(true);

        Assertions.assertThrows(EnrollmentAlreadyExistsException.class, () -> sessionService.enroll(sessionUuid, student.getId()));

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void shouldPromoteFirstWaitlistedWhenConfirmedEnrollmentIsCancelled() {

        UUID enrollmentUuid = UUID.randomUUID();

        User student = new User();
        student.setId(UUID.randomUUID());

        User promotedStudent = new User();
        promotedStudent.setId(UUID.randomUUID());

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 2, true, new TutoringBond(new User(), new Subject()));

        Enrollment enrollment = new Enrollment(EnrollmentStatus.CONFIRMED, session, student);
        enrollment.setId(enrollmentUuid);

        Enrollment firstWaiting = new Enrollment(EnrollmentStatus.WAITLISTED, session, promotedStudent);
        firstWaiting.setQueuePosition(1);
        Enrollment secondWaiting = new Enrollment(EnrollmentStatus.WAITLISTED, session, new User());
        secondWaiting.setQueuePosition(2);
        Enrollment thirdWaiting = new Enrollment(EnrollmentStatus.WAITLISTED, session, new User());
        thirdWaiting.setQueuePosition(3);
        List<Enrollment> waitlist = List.of(firstWaiting, secondWaiting, thirdWaiting);

        when(enrollmentRepository.findById(enrollmentUuid)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.findBySessionAndStatusOrderByQueuePositionAsc(session, EnrollmentStatus.WAITLISTED)).thenReturn(waitlist);
        when(enrollmentRepository.save(any(Enrollment.class))).then(returnsFirstArg());

        Enrollment enrollmentTest = sessionService.cancelEnrollment(enrollmentUuid, student.getId());

        Assertions.assertEquals(EnrollmentStatus.CANCELLED, enrollmentTest.getStatus());
        Assertions.assertNull(enrollmentTest.getQueuePosition());

        Assertions.assertEquals(EnrollmentStatus.CONFIRMED, firstWaiting.getStatus());
        Assertions.assertNull(firstWaiting.getQueuePosition());
        Assertions.assertEquals(1, secondWaiting.getQueuePosition());
        Assertions.assertEquals(2, thirdWaiting.getQueuePosition());

        ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notification.capture());

        Assertions.assertEquals(NotificationEventType.SPOT_PROMOTED, notification.getValue().getEventType());
        Assertions.assertEquals(promotedStudent, notification.getValue().getReceiver());

        verify(enrollmentRepository).saveAll(waitlist);
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void shouldCancelConfirmedEnrollmentWithoutPromotionWhenWaitlistIsEmpty() {

        UUID enrollmentUuid = UUID.randomUUID();

        User student = new User();
        student.setId(UUID.randomUUID());

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 2, true, new TutoringBond(new User(), new Subject()));

        Enrollment enrollment = new Enrollment(EnrollmentStatus.CONFIRMED, session, student);
        enrollment.setId(enrollmentUuid);

        when(enrollmentRepository.findById(enrollmentUuid)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.findBySessionAndStatusOrderByQueuePositionAsc(session, EnrollmentStatus.WAITLISTED)).thenReturn(List.of());
        when(enrollmentRepository.save(any(Enrollment.class))).then(returnsFirstArg());

        Enrollment enrollmentTest = sessionService.cancelEnrollment(enrollmentUuid, student.getId());

        Assertions.assertEquals(EnrollmentStatus.CANCELLED, enrollmentTest.getStatus());

        verifyNoInteractions(notificationRepository);
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void shouldReorderWaitlistWhenWaitlistedEnrollmentIsCancelled() {

        UUID enrollmentUuid = UUID.randomUUID();

        User student = new User();
        student.setId(UUID.randomUUID());

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 2, true, new TutoringBond(new User(), new Subject()));

        Enrollment enrollment = new Enrollment(EnrollmentStatus.WAITLISTED, session, student);
        enrollment.setId(enrollmentUuid);
        enrollment.setQueuePosition(1);

        Enrollment secondWaiting = new Enrollment(EnrollmentStatus.WAITLISTED, session, new User());
        secondWaiting.setQueuePosition(2);
        Enrollment thirdWaiting = new Enrollment(EnrollmentStatus.WAITLISTED, session, new User());
        thirdWaiting.setQueuePosition(3);
        List<Enrollment> waitlist = List.of(secondWaiting, thirdWaiting);

        when(enrollmentRepository.findById(enrollmentUuid)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.findBySessionAndStatusOrderByQueuePositionAsc(session, EnrollmentStatus.WAITLISTED)).thenReturn(waitlist);
        when(enrollmentRepository.save(any(Enrollment.class))).then(returnsFirstArg());

        Enrollment enrollmentTest = sessionService.cancelEnrollment(enrollmentUuid, student.getId());

        Assertions.assertEquals(EnrollmentStatus.CANCELLED, enrollmentTest.getStatus());
        Assertions.assertNull(enrollmentTest.getQueuePosition());

        Assertions.assertEquals(EnrollmentStatus.WAITLISTED, secondWaiting.getStatus());
        Assertions.assertEquals(1, secondWaiting.getQueuePosition());
        Assertions.assertEquals(EnrollmentStatus.WAITLISTED, thirdWaiting.getStatus());
        Assertions.assertEquals(2, thirdWaiting.getQueuePosition());

        verifyNoInteractions(notificationRepository);
        verify(enrollmentRepository).saveAll(waitlist);
    }

    @Test
    void shouldThrowEnrollmentNotFoundExceptionWhenEnrollmentDoesNotExist() {

        UUID enrollmentUuid = UUID.randomUUID();

        when(enrollmentRepository.findById(enrollmentUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(EnrollmentNotFoundException.class, () -> sessionService.cancelEnrollment(enrollmentUuid, UUID.randomUUID()));

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void shouldThrowUserWithoutPermissionExceptionWhenUserIsNotEnrollmentStudent() {

        UUID enrollmentUuid = UUID.randomUUID();

        User student = new User();
        student.setId(UUID.randomUUID());

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 2, true, new TutoringBond(new User(), new Subject()));

        Enrollment enrollment = new Enrollment(EnrollmentStatus.CONFIRMED, session, student);
        enrollment.setId(enrollmentUuid);

        when(enrollmentRepository.findById(enrollmentUuid)).thenReturn(Optional.of(enrollment));

        Assertions.assertThrows(UserWithoutPermissionException.class, () -> sessionService.cancelEnrollment(enrollmentUuid, UUID.randomUUID()));

        Assertions.assertEquals(EnrollmentStatus.CONFIRMED, enrollment.getStatus());

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void shouldThrowInvalidEnrollmentStatusExceptionWhenEnrollmentIsAlreadyCancelled() {

        UUID enrollmentUuid = UUID.randomUUID();

        User student = new User();
        student.setId(UUID.randomUUID());

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        Session session = new Session(startTime, startTime.plusHours(2), 2, true, new TutoringBond(new User(), new Subject()));

        Enrollment enrollment = new Enrollment(EnrollmentStatus.CANCELLED, session, student);
        enrollment.setId(enrollmentUuid);

        when(enrollmentRepository.findById(enrollmentUuid)).thenReturn(Optional.of(enrollment));

        Assertions.assertThrows(InvalidEnrollmentStatusException.class, () -> sessionService.cancelEnrollment(enrollmentUuid, student.getId()));

        verify(enrollmentRepository, never()).findBySessionAndStatusOrderByQueuePositionAsc(any(Session.class), any(EnrollmentStatus.class));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
        verifyNoInteractions(notificationRepository);
    }

}
