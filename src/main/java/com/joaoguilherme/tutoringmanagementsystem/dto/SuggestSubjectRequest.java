package com.joaoguilherme.tutoringmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SuggestSubjectRequest(@NotNull UUID userUuid, @NotBlank @Size(max = 255) String subjectName) {
}
