package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidTimeException;
import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidTutoringBondStatusException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
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

}
