package com.clinica.mariana.restms.service.model;

import java.util.UUID;

public record ServiceModel(UUID id, String categoryCode, String categoryName, UUID createdByUserId, String name,
		String description, Integer estimatedDurationMinutes, boolean active) {
	public ServiceModel {
		if (categoryCode == null || categoryCode.isBlank()) {
			throw new IllegalArgumentException("categoryCode is required");
		}

		name = requireNotBlank(name, "name");

		if (estimatedDurationMinutes != null && estimatedDurationMinutes <= 0) {
			throw new IllegalArgumentException("estimatedDurationMinutes must be greater than 0");
		}
	}

	private static String requireNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank())
			throw new IllegalArgumentException(fieldName + " is required");
		return value;
	}
}
