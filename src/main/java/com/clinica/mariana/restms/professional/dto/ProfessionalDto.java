package com.clinica.mariana.restms.professional.dto;

import java.util.UUID;

public record ProfessionalDto(UUID id, UUID userId, String fullName, UUID clinicId, UUID specialtyId,
		String licenseNumber, boolean active) {
}
