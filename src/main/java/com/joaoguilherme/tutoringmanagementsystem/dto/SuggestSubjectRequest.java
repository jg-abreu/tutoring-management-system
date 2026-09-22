package com.joaoguilherme.tutoringmanagementsystem.dto;

import java.util.UUID;

public record SuggestSubjectRequest (UUID userUuid, String subjectName){
}
