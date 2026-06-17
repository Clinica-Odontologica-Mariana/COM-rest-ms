package com.clinica.mariana.restms.treatment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TreatmentPlanItemDto(UUID id, UUID treatmentPlanId, UUID procedureId, Integer toothNumber,
		String description, String category, BigDecimal estimatedPrice, String status, Integer sortOrder,
		OffsetDateTime completedAt, OffsetDateTime createdAt, List<MaterialItemDto> materials) {
}
