package com.clinica.mariana.restms.medicalrecord.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MedicalRecordAttachmentCreateDto(
		@NotNull(message = "storedFileId is required")
		UUID storedFileId,
		String description
) {
}
