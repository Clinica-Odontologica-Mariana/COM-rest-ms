package com.clinica.mariana.restms.treatment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record TreatmentPlanUpdateDto(UUID professionalId,

		@NotBlank @Size(max = 150) String title,

		@Pattern(regexp = "^(DRAFT|ACTIVE|COMPLETED|CANCELLED)$") String status,

		String notes,

		@DecimalMin("0.00") BigDecimal totalAmount) {
}
