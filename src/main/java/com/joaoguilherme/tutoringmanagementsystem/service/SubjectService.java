package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.exception.UserNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.model.Subject;
import com.joaoguilherme.tutoringmanagementsystem.model.User;
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

}
