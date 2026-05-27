package com.clinica.mariana.restms.clinic.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClinicDto(UUID id, UUID addressId, String name, String document, String phone, String email,
		String timezone, String description, boolean active, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
