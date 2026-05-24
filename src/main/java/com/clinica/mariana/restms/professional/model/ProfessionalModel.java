package com.clinica.mariana.restms.professional.model;

import java.util.UUID;

public record ProfessionalModel(UUID id, UUID userId, UUID clinicId, UUID specialtyId, String licenseNumber,
		boolean active) {
}
