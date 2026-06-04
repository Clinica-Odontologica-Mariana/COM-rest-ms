package com.clinica.mariana.restms.professional.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProfessionalUpdateDto(@NotNull(message = "userId is required") UUID userId,

		@NotNull(message = "clinicId is required") UUID clinicId,

		@NotNull(message = "specialtyId is required") UUID specialtyId,

		@NotBlank(message = "licenseNumber is required") @Size(max = 50, message = "licenseNumber must have at most 50 characters") String licenseNumber) {
}
