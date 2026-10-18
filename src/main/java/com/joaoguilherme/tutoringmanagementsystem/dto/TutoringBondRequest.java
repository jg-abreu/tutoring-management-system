package com.joaoguilherme.tutoringmanagementsystem.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TutoringBondRequest(@NotNull UUID subjectUuid, @NotNull UUID requesterUuid) {
}
