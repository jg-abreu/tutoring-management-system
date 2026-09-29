package com.joaoguilherme.tutoringmanagementsystem.dto;

import java.util.UUID;

public record TutoringBondRequest(UUID subjectUuid, UUID requesterUuid) {
}
