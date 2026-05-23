package com.clinica.mariana.restms.medicalrecord.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MedicalRecordNoteDto(
		UUID id,
		UUID medicalRecordId,
		UUID createdByUserId,
		String note,
		OffsetDateTime createdAt
) {
}
