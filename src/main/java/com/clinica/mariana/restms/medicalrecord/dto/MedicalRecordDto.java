package com.clinica.mariana.restms.medicalrecord.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MedicalRecordDto(
		UUID id,
		UUID patientId,
		UUID createdByUserId,
		String allergies,
		String chronicConditions,
		String continuousMedications,
		String generalObservations,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
