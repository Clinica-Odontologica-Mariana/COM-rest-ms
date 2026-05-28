package com.clinica.mariana.restms.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ServiceCreateDto(

		@NotNull(message = "categoryId is required") UUID categoryId,

		@NotBlank(message = "name is required") @Size(max = 150, message = "name must have at most 150 characters") String name,

		String description,

		@Min(value = 1, message = "estimatedDurationMinutes must be greater than 0") Integer estimatedDurationMinutes) {
}
