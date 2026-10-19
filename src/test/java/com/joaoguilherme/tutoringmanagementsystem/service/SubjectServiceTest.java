package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidSubjectStatusException;
import com.joaoguilherme.tutoringmanagementsystem.exception.SubjectAlreadyExistsException;
import com.joaoguilherme.tutoringmanagementsystem.exception.SubjectNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.exception.UserNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.model.Subject;
import com.joaoguilherme.tutoringmanagementsystem.model.User;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.SubjectStatus;
import com.joaoguilherme.tutoringmanagementsystem.repository.SubjectRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubjectService subjectService;

    private User newUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }

    private Subject newSubject(SubjectStatus status) {
        Subject subject = new Subject(newUser(), "Calculus I");
        subject.setId(UUID.randomUUID());
        subject.setStatus(status);
        return subject;
    }

    @Test
    void shouldSuggestSubjectWhenUserExistsAndNameIsAvailable() {
        User creator = newUser();

        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(subjectRepository.existsByName("Linear Algebra")).thenReturn(false);
        when(subjectRepository.save(any(Subject.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Subject subject = subjectService.suggestSubject(creator.getId(), "Linear Algebra");

        Assertions.assertEquals("Linear Algebra", subject.getName());
        Assertions.assertEquals(creator, subject.getCreator());
        Assertions.assertEquals(SubjectStatus.PENDING, subject.getStatus());
        Assertions.assertNull(subject.getEvaluator());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenCreatorDoesNotExist() {
        UUID creatorUuid = UUID.randomUUID();

        when(userRepository.findById(creatorUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> subjectService.suggestSubject(creatorUuid, "Linear Algebra"));

        verifyNoInteractions(subjectRepository);
    }

    @Test
    void shouldThrowSubjectAlreadyExistsExceptionWhenNameAlreadyExists() {
        User creator = newUser();

        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(subjectRepository.existsByName("Linear Algebra")).thenReturn(true);

        Assertions.assertThrows(SubjectAlreadyExistsException.class, () -> subjectService.suggestSubject(creator.getId(), "Linear Algebra"));

        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void shouldApproveSubjectWhenSubjectIsPending() {
        Subject subject = newSubject(SubjectStatus.PENDING);
        User admin = newUser();

        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(subjectRepository.save(any(Subject.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Subject approved = subjectService.approveSubject(subject.getId(), admin.getId());

        Assertions.assertEquals(SubjectStatus.APPROVED, approved.getStatus());
        Assertions.assertEquals(admin, approved.getEvaluator());
    }

    @Test
    void shouldThrowSubjectNotFoundExceptionWhenApprovingUnknownSubject() {
        UUID subjectUuid = UUID.randomUUID();

        when(subjectRepository.findById(subjectUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(SubjectNotFoundException.class, () -> subjectService.approveSubject(subjectUuid, UUID.randomUUID()));

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowInvalidSubjectStatusExceptionWhenApprovingNonPendingSubject() {
        Subject subject = newSubject(SubjectStatus.REJECTED);

        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));

        Assertions.assertThrows(InvalidSubjectStatusException.class, () -> subjectService.approveSubject(subject.getId(), UUID.randomUUID()));

        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenAdminDoesNotExistOnApprove() {
        Subject subject = newSubject(SubjectStatus.PENDING);
        UUID adminUuid = UUID.randomUUID();

        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        when(userRepository.findById(adminUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> subjectService.approveSubject(subject.getId(), adminUuid));

        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void shouldRejectSubjectWhenSubjectIsPending() {
        Subject subject = newSubject(SubjectStatus.PENDING);
        User admin = newUser();

        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(subjectRepository.save(any(Subject.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Subject rejected = subjectService.rejectSubject(subject.getId(), admin.getId());

        Assertions.assertEquals(SubjectStatus.REJECTED, rejected.getStatus());
        Assertions.assertEquals(admin, rejected.getEvaluator());
    }

    @Test
    void shouldThrowSubjectNotFoundExceptionWhenRejectingUnknownSubject() {
        UUID subjectUuid = UUID.randomUUID();

        when(subjectRepository.findById(subjectUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(SubjectNotFoundException.class, () -> subjectService.rejectSubject(subjectUuid, UUID.randomUUID()));

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowInvalidSubjectStatusExceptionWhenRejectingNonPendingSubject() {
        Subject subject = newSubject(SubjectStatus.APPROVED);

        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));

        Assertions.assertThrows(InvalidSubjectStatusException.class, () -> subjectService.rejectSubject(subject.getId(), UUID.randomUUID()));

        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenAdminDoesNotExistOnReject() {
        Subject subject = newSubject(SubjectStatus.PENDING);
        UUID adminUuid = UUID.randomUUID();

        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        when(userRepository.findById(adminUuid)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> subjectService.rejectSubject(subject.getId(), adminUuid));

        verify(subjectRepository, never()).save(any(Subject.class));
    }
}
