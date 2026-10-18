package com.joaoguilherme.tutoringmanagementsystem.controller;

import com.joaoguilherme.tutoringmanagementsystem.dto.UserActionRequest;
import com.joaoguilherme.tutoringmanagementsystem.model.Enrollment;
import com.joaoguilherme.tutoringmanagementsystem.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("enrollments")
public class EnrollmentController {

    private final SessionService sessionService;

    @PatchMapping("/{enrollmentUuid}/cancel")
    public Enrollment cancelEnrollment (@PathVariable UUID enrollmentUuid, @RequestBody UserActionRequest request) {

        return sessionService.cancelEnrollment(enrollmentUuid, request.userUuid());
    }
}
