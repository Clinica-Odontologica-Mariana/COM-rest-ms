package com.clinica.mariana.restms.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryItemCreateDto(

		@NotNull(message = "clinicId is required") UUID clinicId,

		@NotBlank(message = "itemType is required") @Pattern(regexp = "^(MATERIAL|EQUIPMENT)$") String itemType,

		@NotBlank(message = "name is required") @Size(max = 150) String name,

		String description,

		@Size(max = 80) String sku,

		@NotBlank(message = "unit is required") @Size(max = 30) String unit,

		@DecimalMin(value = "0.00") BigDecimal minimumQuantity) {
}
