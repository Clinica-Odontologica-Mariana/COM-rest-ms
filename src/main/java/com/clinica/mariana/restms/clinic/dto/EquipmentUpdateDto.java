package com.clinica.mariana.restms.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipmentUpdateDto(

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must have at most 150 characters")
        String name,

        String description,

        @Size(max = 150, message = "location must have at most 150 characters")
        String location
) {
}