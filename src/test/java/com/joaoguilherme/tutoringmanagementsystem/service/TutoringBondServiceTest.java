package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidTutoringBondStatusException;
import com.joaoguilherme.tutoringmanagementsystem.exception.TutoringBondNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.exception.UserNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.model.Subject;
import com.joaoguilherme.tutoringmanagementsystem.model.TutoringBond;
import com.joaoguilherme.tutoringmanagementsystem.model.User;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.BondStatus;
import com.joaoguilherme.tutoringmanagementsystem.repository.SubjectRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.TutoringBondRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TutoringBondServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TutoringBondRepository tutoringBondRepository;

    @Mock
    private SubjectRepository subjectRepository;

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
}
