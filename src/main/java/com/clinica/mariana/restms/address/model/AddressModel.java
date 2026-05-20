package com.clinica.mariana.restms.address.model;

import java.util.Locale;
import java.util.UUID;

public record AddressModel(
		UUID id,
		String street,
		String number,
		String complement,
		String neighborhood,
		String city,
		String state,
		String zipCode
) {
	public AddressModel {
		street = requireNotBlank(street, "street");
		city = requireNotBlank(city, "city");
		state = requireNotBlank(state, "state").toUpperCase(Locale.ROOT);
		zipCode = requireNotBlank(zipCode, "zipCode");

		if (!state.matches("^[A-Z]{2}$")) {
			throw new IllegalArgumentException("state must contain exactly 2 uppercase letters");
		}

		if (!zipCode.matches("^[0-9]{8}$")) {
			throw new IllegalArgumentException("zipCode must contain exactly 8 digits");
		}
	}

	public static AddressModel create(
			String street,
			String number,
			String complement,
			String neighborhood,
			String city,
			String state,
			String zipCode
	) {
		return new AddressModel(null, street, number, complement, neighborhood, city, state, zipCode);
	}

	public AddressModel withId(UUID id) {
		return new AddressModel(id, street, number, complement, neighborhood, city, state, zipCode);
	}

	private static String requireNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}

		return value;
	}
}
