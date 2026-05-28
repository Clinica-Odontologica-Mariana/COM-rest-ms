package com.clinica.mariana.restms.clinic.model;

import java.util.UUID;

public record EquipmentModel(UUID id, UUID clinicId, String name, String description, String location, boolean active) {

	public EquipmentModel {
		if (clinicId == null) {
			throw new IllegalArgumentException("clinicId is required");
		}

		name = requireNotBlank(name, "name");
	}

	public static EquipmentModel create(UUID clinicId, String name, String description, String location) {
		return new EquipmentModel(null, clinicId, name, description, location, true);
	}

	public EquipmentModel withId(UUID id) {
		return new EquipmentModel(id, clinicId, name, description, location, active);
	}

	private static String requireNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}

		return value;
	}
}
