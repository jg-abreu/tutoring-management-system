package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidSessionStatusException;
import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidTimeException;
import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidTutoringBondStatusException;
import com.joaoguilherme.tutoringmanagementsystem.exception.SessionNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.exception.TutoringBondNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.exception.UserWithoutPermissionException;
import com.joaoguilherme.tutoringmanagementsystem.model.Session;
import com.joaoguilherme.tutoringmanagementsystem.model.Subject;
import com.joaoguilherme.tutoringmanagementsystem.model.TutoringBond;
import com.joaoguilherme.tutoringmanagementsystem.model.User;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.BondStatus;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.SessionStatus;
import com.joaoguilherme.tutoringmanagementsystem.repository.SessionRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.TutoringBondRepository;
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

}
