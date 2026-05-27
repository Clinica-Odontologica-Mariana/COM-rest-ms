package com.clinica.mariana.restms.odontogram.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OdontogramEntryUpdateDto(

		@NotNull(message = "toothNumber is required") Integer toothNumber,

		@Size(max = 20) String surfaceCode,

		@NotBlank(message = "conditionCode is required") @Size(max = 50) String conditionCode,

		String notes,

		UUID recordedByProfessionalId) {
}
