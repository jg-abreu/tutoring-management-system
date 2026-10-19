package com.joaoguilherme.tutoringmanagementsystem.controller;

import com.joaoguilherme.tutoringmanagementsystem.dto.AdminActionRequest;
import com.joaoguilherme.tutoringmanagementsystem.dto.SuggestSubjectRequest;
import com.joaoguilherme.tutoringmanagementsystem.model.Subject;
import com.joaoguilherme.tutoringmanagementsystem.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Subject suggestSubject(@Valid @RequestBody SuggestSubjectRequest request) {
        return subjectService.suggestSubject(request.userUuid(), request.subjectName());
    }

    @PatchMapping("/{subjectUuid}/approve")
    public Subject approveSubject(@PathVariable UUID subjectUuid, @Valid @RequestBody AdminActionRequest request) {
        return subjectService.approveSubject(subjectUuid, request.adminUuid());
    }

    @PatchMapping("/{subjectUuid}/reject")
    public Subject rejectSubject(@PathVariable UUID subjectUuid, @Valid @RequestBody AdminActionRequest request) {
        return subjectService.rejectSubject(subjectUuid, request.adminUuid());
    }
}
