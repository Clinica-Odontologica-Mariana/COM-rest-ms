package com.clinica.mariana.restms.medicalrecord.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record MedicalRecordModel(
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
