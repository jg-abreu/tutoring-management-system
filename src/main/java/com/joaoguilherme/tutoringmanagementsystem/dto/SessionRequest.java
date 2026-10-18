package com.joaoguilherme.tutoringmanagementsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionRequest(@NotNull UUID tutoringBondUuid, @NotNull UUID userUuid, @NotNull OffsetDateTime startTime, @NotNull OffsetDateTime endTime, @Min(1) int spots, boolean allowsWaitlist) {
}
