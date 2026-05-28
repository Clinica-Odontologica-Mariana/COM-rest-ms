package com.clinica.mariana.restms.clinic.dto;

import java.util.UUID;

public record ClinicDto(UUID id, UUID addressId, String name, String document, String phone, String email,
		String timezone, boolean active) {
}
