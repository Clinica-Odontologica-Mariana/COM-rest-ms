package com.clinica.mariana.restms.inventory.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InventoryItemDto(UUID id, UUID clinicId, String itemType, String name, String description, String sku,
		String unit, BigDecimal currentQuantity, BigDecimal minimumQuantity, boolean active, OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
}
