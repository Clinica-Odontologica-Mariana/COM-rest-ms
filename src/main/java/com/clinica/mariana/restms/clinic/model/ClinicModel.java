package com.clinica.mariana.restms.clinic.model;

import java.util.UUID;

public record ClinicModel(UUID id, UUID addressId, String name, String phone, String email, String timezone,
		boolean active) {
	public ClinicModel {
		name = requireNotBlank(name, "name");
		phone = requireNotBlank(phone, "phone");
		timezone = (timezone == null || timezone.isBlank()) ? "America/Sao_Paulo" : timezone;

		if (email != null && !email.isBlank() && !email.matches("(?i)^[A-Z0-9._%+\\-]+@[A-Z0-9.\\-]+\\.[A-Z]{2,}$")) {
			throw new IllegalArgumentException("email format is invalid");
		}
	}

	public static ClinicModel create(UUID addressId, String name, String phone, String email, String timezone) {
		return new ClinicModel(null, addressId, name, phone, email, timezone, true);
	}

	public ClinicModel withId(UUID id) {
		return new ClinicModel(id, addressId, name, phone, email, timezone, active);
	}

	private static String requireNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		return value;
	}
}
