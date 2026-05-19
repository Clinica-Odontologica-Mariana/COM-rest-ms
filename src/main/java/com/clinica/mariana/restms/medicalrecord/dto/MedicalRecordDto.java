package com.clinica.mariana.restms.medicalrecord.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MedicalRecordDto(
		UUID id,
		UUID patientId,
		String patientFullName,
		String allergies,
		String chronicConditions,
		String continuousMedications,
		String generalObservations,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
