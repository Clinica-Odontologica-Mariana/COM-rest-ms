package com.clinica.mariana.restms.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ServiceCostDto(UUID id, UUID serviceId, String costTypeCode, String costTypeName, BigDecimal amount,
		String description, LocalDate validFrom, LocalDate validTo) {
}
