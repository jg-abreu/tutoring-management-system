package com.joaoguilherme.tutoringmanagementsystem.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionRequest(UUID tutoringBondUuid, UUID userUuid, OffsetDateTime startTime, OffsetDateTime endTime, int spots, boolean allowsWaitlist) {
}
