package com.clinica.mariana.restms.service.dto;

import java.util.UUID;

public record ServiceDto(UUID id, String categoryCode, String categoryName, String name, String description,
		Integer estimatedDurationMinutes, boolean active) {
}
