package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidSubjectStatusException;
import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidTutoringBondStatusException;
import com.joaoguilherme.tutoringmanagementsystem.exception.SubjectNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.exception.TutoringBondAlreadyExistsException;
import com.joaoguilherme.tutoringmanagementsystem.exception.TutoringBondNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.exception.UserNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.model.Subject;
import com.joaoguilherme.tutoringmanagementsystem.model.TutoringBond;
import com.joaoguilherme.tutoringmanagementsystem.model.User;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.BondStatus;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.SubjectStatus;
import com.joaoguilherme.tutoringmanagementsystem.repository.SubjectRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.TutoringBondRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TutoringBondServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TutoringBondRepository tutoringBondRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private TutoringBondService tutoringBondService;

    @Test
    void shouldApproveTutoringBondWhenStatusIsPending() {
        UUID tutoringBondUuid = UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID());

        Subject subject = new Subject();
        subject.setId(UUID.randomUUID());

        TutoringBond tutoringBond = new TutoringBond(user, subject);
        tutoringBond.setId(UUID.randomUUID());

        User admin = new User();
        admin.setId(UUID.randomUUID());

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(tutoringBondRepository.save(any(TutoringBond.class))).thenReturn(tutoringBond);

        TutoringBond tutoringBondTest = tutoringBondService.approveTutoringBond(tutoringBondUuid, admin.getId());

        Assertions.assertEquals(BondStatus.APPROVED, tutoringBondTest.getStatus());

        Assertions.assertEquals(admin, tutoringBondTest.getEvaluator());
    }

    @Test
    void shouldThrowTutoringBondNotFoundExceptionWhenBondDoesNotExist() {
        UUID tutoringBondUuid = UUID.randomUUID();

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(TutoringBondNotFoundException.class, () -> tutoringBondService.approveTutoringBond(tutoringBondUuid, UUID.randomUUID()));    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenEvaluatorDoesNotExist() {
        UUID tutoringBondUuid = UUID.randomUUID();

        TutoringBond tutoringBond = new TutoringBond(new User(), new Subject());
        tutoringBond.setId(tutoringBondUuid);

        UUID evaluatorUuid = UUID.randomUUID();

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(userRepository.findById(evaluatorUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> tutoringBondService.approveTutoringBond(tutoringBondUuid, evaluatorUuid));
    }

    @Test
    void shouldThrowInvalidTutoringBondStatusExceptionWhenStatusIsNotPending() {
        UUID tutoringBondUuid = UUID.randomUUID();

        TutoringBond tutoringBond = new TutoringBond(new User(), new Subject());
        tutoringBond.setId(tutoringBondUuid);
        tutoringBond.setStatus(BondStatus.APPROVED);

        User admin = new User();
        admin.setId(UUID.randomUUID());

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        Assertions.assertThrows(InvalidTutoringBondStatusException.class, () -> tutoringBondService.approveTutoringBond(tutoringBondUuid, admin.getId()));
    }

    @Test
    void shouldRejectTutoringBondWhenStatusIsPending() {
        UUID tutoringBondUuid = UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID());

        Subject subject = new Subject();
        subject.setId(UUID.randomUUID());

        TutoringBond tutoringBond = new TutoringBond(user, subject);
        tutoringBond.setId(UUID.randomUUID());

        User admin = new User();
        admin.setId(UUID.randomUUID());

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(tutoringBondRepository.save(any(TutoringBond.class))).thenReturn(tutoringBond);

        TutoringBond tutoringBondTest = tutoringBondService.rejectTutoringBond(tutoringBondUuid, admin.getId());

        Assertions.assertEquals(BondStatus.REJECTED, tutoringBondTest.getStatus());

        Assertions.assertEquals(admin, tutoringBondTest.getEvaluator());
    }

    @Test
    void shouldThrowTutoringBondNotFoundExceptionWhenRejectingNonexistentBond() {
        UUID tutoringBondUuid = UUID.randomUUID();

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(TutoringBondNotFoundException.class, () -> tutoringBondService.rejectTutoringBond(tutoringBondUuid, UUID.randomUUID()));    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenRejectingWithNonexistentEvaluator() {
        UUID tutoringBondUuid = UUID.randomUUID();

        TutoringBond tutoringBond = new TutoringBond(new User(), new Subject());
        tutoringBond.setId(tutoringBondUuid);

        UUID evaluatorUuid = UUID.randomUUID();

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(userRepository.findById(evaluatorUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> tutoringBondService.rejectTutoringBond(tutoringBondUuid, evaluatorUuid));
    }

    @Test
    void shouldThrowInvalidTutoringBondStatusExceptionWhenRejectingNonPendingBond() {
        UUID tutoringBondUuid = UUID.randomUUID();

        TutoringBond tutoringBond = new TutoringBond(new User(), new Subject());
        tutoringBond.setId(tutoringBondUuid);
        tutoringBond.setStatus(BondStatus.APPROVED);

        User admin = new User();
        admin.setId(UUID.randomUUID());

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        Assertions.assertThrows(InvalidTutoringBondStatusException.class, () -> tutoringBondService.rejectTutoringBond(tutoringBondUuid, admin.getId()));
    }

    @Test
    void shouldRevokeTutoringBondWhenStatusIsApproved() {
        UUID tutoringBondUuid = UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID());

        Subject subject = new Subject();
        subject.setId(UUID.randomUUID());

        TutoringBond tutoringBond = new TutoringBond(user, subject);
        tutoringBond.setId(UUID.randomUUID());
        tutoringBond.setStatus(BondStatus.APPROVED);

        User admin = new User();
        admin.setId(UUID.randomUUID());

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(tutoringBondRepository.save(any(TutoringBond.class))).thenReturn(tutoringBond);

        TutoringBond tutoringBondTest = tutoringBondService.revokeTutoringBond(tutoringBondUuid, admin.getId());

        Assertions.assertEquals(BondStatus.REVOKED, tutoringBondTest.getStatus());

        Assertions.assertEquals(admin, tutoringBondTest.getEvaluator());
    }

    @Test
    void shouldThrowTutoringBondNotFoundExceptionWhenRevokingNonexistentBond() {
        UUID tutoringBondUuid = UUID.randomUUID();

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(TutoringBondNotFoundException.class, () -> tutoringBondService.revokeTutoringBond(tutoringBondUuid, UUID.randomUUID()));    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenRevokingWithNonexistentEvaluator() {
        UUID tutoringBondUuid = UUID.randomUUID();

        TutoringBond tutoringBond = new TutoringBond(new User(), new Subject());
        tutoringBond.setId(tutoringBondUuid);

        UUID evaluatorUuid = UUID.randomUUID();

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(userRepository.findById(evaluatorUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> tutoringBondService.revokeTutoringBond(tutoringBondUuid, evaluatorUuid));
    }

    @Test
    void shouldThrowInvalidTutoringBondStatusExceptionWhenRevokingNonApprovedBond() {
        UUID tutoringBondUuid = UUID.randomUUID();

        TutoringBond tutoringBond = new TutoringBond(new User(), new Subject());
        tutoringBond.setId(tutoringBondUuid);
        tutoringBond.setStatus(BondStatus.REVOKED);

        User admin = new User();
        admin.setId(UUID.randomUUID());

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        Assertions.assertThrows(InvalidTutoringBondStatusException.class, () -> tutoringBondService.revokeTutoringBond(tutoringBondUuid, admin.getId()));
    }

    @Test
    void shouldCancelFutureSessionsWhenTutoringBondIsRevoked() {
        UUID tutoringBondUuid = UUID.randomUUID();

        TutoringBond tutoringBond = new TutoringBond(new User(), new Subject());
        tutoringBond.setId(tutoringBondUuid);
        tutoringBond.setStatus(BondStatus.APPROVED);

        User admin = new User();
        admin.setId(UUID.randomUUID());

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(tutoringBondRepository.save(any(TutoringBond.class))).thenReturn(tutoringBond);

        tutoringBondService.revokeTutoringBond(tutoringBondUuid, admin.getId());

        verify(sessionService).cancelFutureSessions(tutoringBond);
    }

    @Test
    void shouldNotCancelSessionsWhenTutoringBondIsNotApproved() {
        UUID tutoringBondUuid = UUID.randomUUID();

        TutoringBond tutoringBond = new TutoringBond(new User(), new Subject());
        tutoringBond.setId(tutoringBondUuid);
        tutoringBond.setStatus(BondStatus.PENDING);

        User admin = new User();
        admin.setId(UUID.randomUUID());

        when(tutoringBondRepository.findById(tutoringBondUuid)).thenReturn(Optional.of(tutoringBond));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        Assertions.assertThrows(InvalidTutoringBondStatusException.class, () -> tutoringBondService.revokeTutoringBond(tutoringBondUuid, admin.getId()));

        verifyNoInteractions(sessionService);
    }

    private Subject subjectWithStatus(SubjectStatus status) {
        Subject subject = new Subject(new User(), "Calculus I");
        subject.setId(UUID.randomUUID());
        subject.setStatus(status);
        return subject;
    }

    @Test
    void shouldRequestTutoringBondWhenSubjectIsApprovedAndNoActiveBondExists() {
        Subject subject = subjectWithStatus(SubjectStatus.APPROVED);

        User requester = new User();
        requester.setId(UUID.randomUUID());

        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(tutoringBondRepository.findByRequesterAndSubjectAndStatusIn(requester, subject, List.of(BondStatus.PENDING, BondStatus.APPROVED))).thenReturn(List.of());
        when(tutoringBondRepository.save(any(TutoringBond.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TutoringBond tutoringBond = tutoringBondService.requestTutoringBond(subject.getId(), requester.getId());

        Assertions.assertEquals(BondStatus.PENDING, tutoringBond.getStatus());
        Assertions.assertEquals(requester, tutoringBond.getRequester());
        Assertions.assertEquals(subject, tutoringBond.getSubject());
        Assertions.assertNull(tutoringBond.getEvaluator());
    }

    @Test
    void shouldThrowSubjectNotFoundExceptionWhenRequestingBondForUnknownSubject() {
        UUID subjectUuid = UUID.randomUUID();

        when(subjectRepository.findById(subjectUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(SubjectNotFoundException.class, () -> tutoringBondService.requestTutoringBond(subjectUuid, UUID.randomUUID()));

        verifyNoInteractions(userRepository, tutoringBondRepository);
    }

    @Test
    void shouldThrowInvalidSubjectStatusExceptionWhenSubjectIsPending() {
        Subject subject = subjectWithStatus(SubjectStatus.PENDING);

        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));

        Assertions.assertThrows(InvalidSubjectStatusException.class, () -> tutoringBondService.requestTutoringBond(subject.getId(), UUID.randomUUID()));

        verifyNoInteractions(userRepository, tutoringBondRepository);
    }

    @Test
    void shouldThrowInvalidSubjectStatusExceptionWhenSubjectIsRejected() {
        Subject subject = subjectWithStatus(SubjectStatus.REJECTED);

        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));

        Assertions.assertThrows(InvalidSubjectStatusException.class, () -> tutoringBondService.requestTutoringBond(subject.getId(), UUID.randomUUID()));

        verifyNoInteractions(userRepository, tutoringBondRepository);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenRequesterDoesNotExist() {
        Subject subject = subjectWithStatus(SubjectStatus.APPROVED);
        UUID requesterUuid = UUID.randomUUID();

        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        when(userRepository.findById(requesterUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> tutoringBondService.requestTutoringBond(subject.getId(), requesterUuid));

        verifyNoInteractions(tutoringBondRepository);
    }

    @Test
    void shouldThrowTutoringBondAlreadyExistsExceptionWhenActiveBondExists() {
        Subject subject = subjectWithStatus(SubjectStatus.APPROVED);

        User requester = new User();
        requester.setId(UUID.randomUUID());

        TutoringBond existingBond = new TutoringBond(requester, subject);

        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(tutoringBondRepository.findByRequesterAndSubjectAndStatusIn(requester, subject, List.of(BondStatus.PENDING, BondStatus.APPROVED))).thenReturn(List.of(existingBond));

        Assertions.assertThrows(TutoringBondAlreadyExistsException.class, () -> tutoringBondService.requestTutoringBond(subject.getId(), requester.getId()));

        verify(tutoringBondRepository, never()).save(any(TutoringBond.class));
    }
}
