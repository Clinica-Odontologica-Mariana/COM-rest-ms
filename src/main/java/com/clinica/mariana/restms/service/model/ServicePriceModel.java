package com.clinica.mariana.restms.service.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ServicePriceModel(UUID id, UUID serviceId, BigDecimal amount, String description, LocalDate validFrom,
		LocalDate validTo) {
	public ServicePriceModel {
		if (serviceId == null) {
			throw new IllegalArgumentException("serviceId is required");
		}

		if (amount == null) {
			throw new IllegalArgumentException("amount is required");
		}

		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("amount must be zero or greater");
		}

		if (validFrom == null) {
			throw new IllegalArgumentException("validFrom is required");
		}

		if (validTo != null && validTo.isBefore(validFrom)) {
			throw new IllegalArgumentException("validTo must not be before validFrom");
		}
	}
}
