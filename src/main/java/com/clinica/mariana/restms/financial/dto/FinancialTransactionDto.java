package com.clinica.mariana.restms.financial.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FinancialTransactionDto(UUID id, UUID clinicId, UUID appointmentId, UUID treatmentPlanId,
		String description, String type, String category, BigDecimal amount, String status, LocalDate transactionDate,
		String notes, UUID createdByUserId, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
