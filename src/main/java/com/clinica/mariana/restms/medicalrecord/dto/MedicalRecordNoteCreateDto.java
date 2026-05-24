package com.clinica.mariana.restms.medicalrecord.dto;

import jakarta.validation.constraints.NotBlank;

public record MedicalRecordNoteCreateDto(@NotBlank(message = "note is required") String note) {
}
