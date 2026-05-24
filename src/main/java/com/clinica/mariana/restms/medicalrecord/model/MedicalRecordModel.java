package com.clinica.mariana.restms.medicalrecord.model;

import java.util.UUID;

public record MedicalRecordModel(UUID id, UUID patientId, UUID createdByUserId, String allergies,
		String chronicConditions, String continuousMedications, String generalObservations) {
}
