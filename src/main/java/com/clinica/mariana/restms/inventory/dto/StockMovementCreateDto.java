package com.clinica.mariana.restms.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record StockMovementCreateDto(

		@NotNull(message = "inventoryItemId is required") UUID inventoryItemId,

		@NotBlank(message = "movementType is required") @Pattern(regexp = "^(IN|OUT|ADJUSTMENT)$") String movementType,

		@NotNull(message = "quantity is required") @DecimalMin(value = "0.01") BigDecimal quantity,

		@Size(max = 255) String reason,

		UUID createdByUserId) {
}
