package com.clinica.mariana.restms.medicalrecord.dto;

import jakarta.validation.constraints.NotBlank;

public record MedicalRecordNoteUpdateDto(
		@NotBlank(message = "note is required")
		String note
) {
}
