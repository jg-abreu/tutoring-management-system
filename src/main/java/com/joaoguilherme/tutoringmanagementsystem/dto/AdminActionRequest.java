package com.joaoguilherme.tutoringmanagementsystem.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AdminActionRequest(@NotNull UUID adminUuid) {
}
