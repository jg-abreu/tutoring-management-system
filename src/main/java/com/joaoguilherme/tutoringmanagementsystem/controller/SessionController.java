package com.joaoguilherme.tutoringmanagementsystem.controller;

import com.joaoguilherme.tutoringmanagementsystem.dto.SessionRequest;
import com.joaoguilherme.tutoringmanagementsystem.dto.UserActionRequest;
import com.joaoguilherme.tutoringmanagementsystem.model.Enrollment;
import com.joaoguilherme.tutoringmanagementsystem.model.Session;
import com.joaoguilherme.tutoringmanagementsystem.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public Session createSession(@RequestBody SessionRequest request) {
        return sessionService.createSession(request.tutoringBondUuid(), request.userUuid(), request.startTime(), request.endTime(), request.spots(), request.allowsWaitlist());
    }

    @PatchMapping("/{sessionUuid}/cancel")
    public Session cancelSession( @PathVariable UUID sessionUuid , @RequestBody UserActionRequest request) {

        return sessionService.cancelSession(sessionUuid, request.userUuid());

    }

    @PostMapping("/{sessionUuid}/enrollments")
    public Enrollment enroll (@PathVariable UUID sessionUuid, @RequestBody UserActionRequest request) {
        return sessionService.enroll(sessionUuid, request.userUuid());
    }
}
