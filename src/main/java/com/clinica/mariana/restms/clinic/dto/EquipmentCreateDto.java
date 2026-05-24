package com.clinica.mariana.restms.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record EquipmentCreateDto(

        @NotNull(message = "clinicId is required")
        UUID clinicId,

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must have at most 150 characters")
        String name,

        String description,

        @Size(max = 100, message = "location must have at most 100 characters")
        String location
) {
}