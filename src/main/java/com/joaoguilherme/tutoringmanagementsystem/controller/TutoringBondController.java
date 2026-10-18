package com.joaoguilherme.tutoringmanagementsystem.controller;

import com.joaoguilherme.tutoringmanagementsystem.dto.AdminActionRequest;
import com.joaoguilherme.tutoringmanagementsystem.dto.TutoringBondRequest;
import com.joaoguilherme.tutoringmanagementsystem.model.TutoringBond;
import com.joaoguilherme.tutoringmanagementsystem.service.TutoringBondService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tutoring-bonds")
public class TutoringBondController {

    private final TutoringBondService tutoringBondService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TutoringBond requestTutoringBond(@Valid @RequestBody TutoringBondRequest request) {
        return tutoringBondService.requestTutoringBond(request.subjectUuid(), request.requesterUuid());
    }

    @PatchMapping("/{tutoringBondUuid}/approve")
    public TutoringBond approveTutoringBond(@PathVariable UUID tutoringBondUuid, @Valid @RequestBody AdminActionRequest adminActionRequest) {
        return tutoringBondService.approveTutoringBond(tutoringBondUuid, adminActionRequest.adminUuid());
    }

    @PatchMapping("/{tutoringBondUuid}/reject")
    public TutoringBond rejectTutoringBond(@PathVariable UUID tutoringBondUuid, @Valid @RequestBody AdminActionRequest adminActionRequest) {
        return tutoringBondService.rejectTutoringBond(tutoringBondUuid, adminActionRequest.adminUuid());
    }

    @PatchMapping("/{tutoringBondUuid}/revoke")
    public TutoringBond revokeTutoringBond(@PathVariable UUID tutoringBondUuid, @Valid @RequestBody AdminActionRequest adminActionRequest) {
        return tutoringBondService.revokeTutoringBond(tutoringBondUuid, adminActionRequest.adminUuid());
    }
}
