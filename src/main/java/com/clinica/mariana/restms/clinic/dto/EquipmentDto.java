package com.clinica.mariana.restms.clinic.dto;

import java.util.UUID;

public record EquipmentDto(
        UUID id,
        UUID clinicId,
        String name,
        String description,
        String location,
        boolean active
) {
}