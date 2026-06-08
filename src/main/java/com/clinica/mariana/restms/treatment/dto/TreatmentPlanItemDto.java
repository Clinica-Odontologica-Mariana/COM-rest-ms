package com.clinica.mariana.restms.treatment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TreatmentPlanItemDto(UUID id, UUID treatmentPlanId, UUID procedureId, Integer toothNumber,
		String description, BigDecimal estimatedPrice, String status, Integer sortOrder, OffsetDateTime completedAt,
		OffsetDateTime createdAt) {
}
