package com.clinica.mariana.restms.workplace.dto;

import java.util.UUID;

public record WorkplaceDto(UUID id, UUID clinicId, String name, String description, boolean active) {
}
