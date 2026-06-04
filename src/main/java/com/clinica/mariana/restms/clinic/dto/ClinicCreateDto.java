package com.clinica.mariana.restms.clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ClinicCreateDto(UUID addressId,

		@NotBlank(message = "name is required") @Size(max = 150) String name,

		@NotBlank(message = "document is required") @Pattern(regexp = "^[0-9]{14}$") String document,

		@NotBlank(message = "phone is required") @Size(max = 20) String phone,

		@Email(message = "email must be valid") @Size(max = 150) String email,

		@Size(max = 80) String timezone,

		String description) {
}
