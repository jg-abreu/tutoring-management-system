package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidSubjectStatusException;
import com.joaoguilherme.tutoringmanagementsystem.exception.SubjectNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.exception.UserNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.model.Subject;
import com.joaoguilherme.tutoringmanagementsystem.model.User;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.SubjectStatus;
import com.joaoguilherme.tutoringmanagementsystem.repository.SubjectRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    private final UserRepository userRepository;

    public Subject suggestSubject(UUID userUuid, String subjectName) {
        User creator = userRepository.findById(userUuid).orElseThrow(() -> new UserNotFoundException("User not found"));

        Subject subject = new Subject(creator, subjectName);

        return subjectRepository.save(subject);
    }

    public Subject approveSubject(UUID adminUuid, UUID subjectUuid) {
        Subject subject = subjectRepository.findById(subjectUuid).orElseThrow(() -> new SubjectNotFoundException("Subject not found"));

        if (!(subject.getStatus().equals(SubjectStatus.PENDING))) {
            throw new InvalidSubjectStatusException("Invalid subject status");
        }
        User admin = userRepository.findById(adminUuid).orElseThrow(() -> new UserNotFoundException("Admin not found"));

        subject.setStatus(SubjectStatus.APPROVED);

        subject.setEvaluator(admin);

        return subjectRepository.save(subject);
    }

    public Subject rejectSubject(UUID adminUuid, UUID subjectUuid) {
        Subject subject = subjectRepository.findById(subjectUuid).orElseThrow(() -> new SubjectNotFoundException("Subject not found"));

        if (!(subject.getStatus().equals(SubjectStatus.PENDING))){
            throw new InvalidSubjectStatusException("Invalid subject exception");
        }

        User admin = userRepository.findById(adminUuid).orElseThrow(() -> new UserNotFoundException("Admin not found"));

        subject.setStatus(SubjectStatus.REJECTED);

        subject.setEvaluator(admin);

        return subjectRepository.save(subject);
    }

}
