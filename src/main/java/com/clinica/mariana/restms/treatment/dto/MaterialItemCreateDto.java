package com.clinica.mariana.restms.treatment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record MaterialItemCreateDto(

		@NotBlank String name,

		String category,

		@Min(1) Integer quantity) {
}
