package com.clinica.mariana.restms.treatment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TreatmentPlanItemUpdateDto(UUID procedureId, Integer toothNumber,

		@NotBlank String description,

		String category,

		@DecimalMin("0.00") BigDecimal estimatedPrice,

		@Pattern(regexp = "^(PENDING|APPROVED|DONE|CANCELLED)$") String status,

		@Min(1) Integer sortOrder,

		@Valid List<MaterialItemCreateDto> materials) {
}
