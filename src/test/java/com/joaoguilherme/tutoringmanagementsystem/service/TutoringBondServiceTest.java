package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.repository.SubjectRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.TutoringBondRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
