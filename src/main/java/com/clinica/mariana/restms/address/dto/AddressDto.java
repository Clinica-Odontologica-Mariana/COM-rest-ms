package com.clinica.mariana.restms.address.dto;

import java.util.UUID;

public record AddressDto(UUID id, String street, String number, String complement, String neighborhood, String city,
		String state, String zipCode) {
}
