package com.joaoguilherme.tutoringmanagementsystem.service;

import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidTimeException;
import com.joaoguilherme.tutoringmanagementsystem.exception.InvalidTutoringBondStatusException;
import com.joaoguilherme.tutoringmanagementsystem.exception.TutoringBondNotFoundException;
import com.joaoguilherme.tutoringmanagementsystem.model.Session;
import com.joaoguilherme.tutoringmanagementsystem.model.TutoringBond;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.BondStatus;
import com.joaoguilherme.tutoringmanagementsystem.repository.SessionRepository;
import com.joaoguilherme.tutoringmanagementsystem.repository.TutoringBondRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final TutoringBondRepository tutoringBondRepository;
    private final SessionRepository sessionRepository;

    public Session createSession(UUID tutoringBondUuid, OffsetDateTime startTime, OffsetDateTime endTime, int spots, boolean allowsWaitList) {

        if (!(endTime.isAfter(startTime))) {
            throw new InvalidTimeException("Invalid Time Exception");
        }

        TutoringBond tutoringBond = tutoringBondRepository.findById(tutoringBondUuid).orElseThrow(() -> new TutoringBondNotFoundException("TutoringBond not found"));

        if (tutoringBond.getStatus() != BondStatus.APPROVED) {
            throw new InvalidTutoringBondStatusException("Invalid tutoringBond status exception");
        }

        Session session = new Session(startTime, endTime, spots, allowsWaitList, tutoringBond);

        return sessionRepository.save(session);
    }

}
