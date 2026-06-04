package com.clinica.mariana.restms.inventory.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record StockMovementDto(UUID id, UUID inventoryItemId, String movementType, BigDecimal quantity, String reason,
		UUID createdByUserId, OffsetDateTime createdAt) {
}
