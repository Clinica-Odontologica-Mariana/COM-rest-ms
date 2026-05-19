package com.clinica.mariana.restms.clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ClinicCreateDto(

        UUID addressId,

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must have at most 150 characters")
        String name,

        @NotBlank(message = "document is required")
        @Pattern(regexp = "^[0-9]{14}$", message = "document must contain exactly 14 digits (CNPJ)")
        String document,

        @NotBlank(message = "phone is required")
        @Size(max = 20, message = "phone must have at most 20 characters")
        String phone,

        @Email(message = "email format is invalid")
        @Size(max = 150, message = "email must have at most 150 characters")
        String email,

        @Size(max = 50, message = "timezone must have at most 50 characters")
        String timezone
) {
}