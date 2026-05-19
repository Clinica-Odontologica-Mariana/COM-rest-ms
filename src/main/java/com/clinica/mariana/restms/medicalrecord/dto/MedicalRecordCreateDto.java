package com.clinica.mariana.restms.medicalrecord.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MedicalRecordCreateDto(
		@NotNull(message = "patientId is required")
		UUID patientId,

		@Size(max = 4000, message = "allergies must have at most 4000 characters")
		String allergies,

		@Size(max = 4000, message = "chronicConditions must have at most 4000 characters")
		String chronicConditions,

		@Size(max = 4000, message = "continuousMedications must have at most 4000 characters")
		String continuousMedications,

		@Size(max = 4000, message = "generalObservations must have at most 4000 characters")
		String generalObservations
) {
}
