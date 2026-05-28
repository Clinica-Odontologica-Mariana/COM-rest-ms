package com.clinica.mariana.restms.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ServiceCostCreateDto(

		@NotNull(message = "serviceId is required") UUID serviceId,

		@NotNull(message = "costTypeId is required") UUID costTypeId,

		@NotNull(message = "amount is required") @DecimalMin(value = "0.0", message = "amount must be zero or greater") BigDecimal amount,

		String description,

		@NotNull(message = "validFrom is required") LocalDate validFrom,

		LocalDate validTo) {
}
