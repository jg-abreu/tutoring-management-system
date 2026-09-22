package com.joaoguilherme.tutoringmanagementsystem.controller;

import com.joaoguilherme.tutoringmanagementsystem.dto.AdminActionRequest;
import com.joaoguilherme.tutoringmanagementsystem.dto.SuggestSubjectRequest;
import com.joaoguilherme.tutoringmanagementsystem.model.Subject;
import com.joaoguilherme.tutoringmanagementsystem.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public Subject suggestSubject (@RequestBody SuggestSubjectRequest request) {
        return subjectService.suggestSubject(request.userUuid(), request.subjectName());
    }

    @PutMapping ("/{subjectUuid}/approve")
    public Subject approveSubject (@PathVariable UUID subjectUuid, @RequestBody AdminActionRequest request) {

        return subjectService.approveSubject(request.adminUuid(), subjectUuid);
    }

    @PutMapping ("/{subjectUuid}/reject")
    public Subject rejectSubject (@PathVariable UUID subjectUuid, @RequestBody AdminActionRequest request) {

        return subjectService.rejectSubject(request.adminUuid(), subjectUuid);
    }
}
