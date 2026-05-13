package com.clinica.mariana.restms.clinic.model;

import java.util.UUID;

public record ClinicModel(
		UUID id,
		UUID addressId,
		String name,
		String document,
		String phone,
		String email,
		String timezone,
		boolean active
) {
	private static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";

	public ClinicModel {
		name = requireNotBlank(name, "name");
		document = requireNotBlank(document, "document");
		phone = requireNotBlank(phone, "phone");
		timezone = (timezone == null || timezone.isBlank()) ? DEFAULT_TIMEZONE : timezone;

		if (!document.matches("^[0-9]{14}$")) {
			throw new IllegalArgumentException("document must contain exactly 14 digits");
		}
	}

	public static ClinicModel create(
			UUID addressId,
			String name,
			String document,
			String phone,
			String email,
			String timezone
	) {
		return new ClinicModel(null, addressId, name, document, phone, email, timezone, true);
	}

	private static String requireNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}

		return value;
	}
}
