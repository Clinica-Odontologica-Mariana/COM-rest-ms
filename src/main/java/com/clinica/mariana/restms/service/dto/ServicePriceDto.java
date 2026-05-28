package com.clinica.mariana.restms.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ServicePriceDto(UUID id, UUID serviceId, BigDecimal amount, String description, LocalDate validFrom,
		LocalDate validTo) {
}
