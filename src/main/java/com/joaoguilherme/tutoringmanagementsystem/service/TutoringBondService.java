package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.exception.SubjectNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.exception.TutoringBondAlreadyExistsException;
import com.joaoguilherme.tutoringmanagementsystem.exception.UserNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.model.Subject;
import com.joaoguilherme.tutoringmanagementsystem.model.TutoringBond;
import com.joaoguilherme.tutoringmanagementsystem.model.User;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.BondStatus;
import com.joaoguilherme.tutoringmanagementsystem.repository.SubjectRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.TutoringBondRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TutoringBondService {

    private final TutoringBondRepository tutoringBondRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public TutoringBond requestTutoringBond (UUID subjectUuid, UUID requesterUuid) {
        Subject subject = subjectRepository.findById(subjectUuid).orElseThrow(() -> new SubjectNotFoundException("Subject not found"));

        User requester = userRepository.findById(requesterUuid).orElseThrow(() -> new UserNotFoundException("User not found"));

        List<TutoringBond> existingBonds = tutoringBondRepository.findByRequesterAndSubjectAndStatusIn(requester, subject, List.of(BondStatus.PENDING, BondStatus.APPROVED));

        if (!existingBonds.isEmpty()) {
            throw new TutoringBondAlreadyExistsException("Tutoring Bond AlreadyExists");
        }

        TutoringBond tutoringBond = new TutoringBond(requester, subject);

        return tutoringBondRepository.save(tutoringBond);

    }
}
