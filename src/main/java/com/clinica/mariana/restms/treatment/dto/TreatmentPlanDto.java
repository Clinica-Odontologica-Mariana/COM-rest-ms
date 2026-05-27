package com.clinica.mariana.restms.treatment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TreatmentPlanDto(UUID id, UUID patientId, UUID medicalRecordId, UUID professionalId, String title,
		String status, String notes, BigDecimal totalAmount, UUID createdByUserId, OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
}
