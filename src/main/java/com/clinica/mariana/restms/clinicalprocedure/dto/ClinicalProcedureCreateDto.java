package com.clinica.mariana.restms.clinicalprocedure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ClinicalProcedureCreateDto(

		@Size(max = 50) String code,

		@NotBlank(message = "name is required") @Size(max = 150) String name,

		@Size(max = 80) String category,

		String description,

		@Min(value = 1, message = "estimatedDurationMinutes must be greater than zero") Integer estimatedDurationMinutes,

		@DecimalMin(value = "0.00", message = "basePrice must be greater than or equal to zero") BigDecimal basePrice) {
}
