package com.clinica.mariana.restms.clinicalprocedure.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ClinicalProcedureDto(UUID id, String code, String name, String category, String description,
		Integer estimatedDurationMinutes, BigDecimal basePrice, boolean active, OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
}
