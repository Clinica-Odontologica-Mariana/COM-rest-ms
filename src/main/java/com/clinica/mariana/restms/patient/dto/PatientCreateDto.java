package com.clinica.mariana.restms.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record PatientCreateDto(
		UUID addressId,

		@NotBlank(message = "fullName is required")
		@Size(max = 150, message = "fullName must have at most 150 characters")
		String fullName,

		@NotBlank(message = "cpf is required")
		@Pattern(regexp = "^[0-9]{11}$", message = "cpf must contain exactly 11 digits")
		String cpf,

		@NotBlank(message = "phone is required")
		@Size(max = 20, message = "phone must have at most 20 characters")
		String phone,

		@Email(message = "email must be valid")
		@Size(max = 150, message = "email must have at most 150 characters")
		String email,

		@NotNull(message = "birthDate is required")
		@PastOrPresent(message = "birthDate must not be in the future")
		LocalDate birthDate,

		@Size(max = 150, message = "emergencyContactName must have at most 150 characters")
		String emergencyContactName,

		@Size(max = 20, message = "emergencyContactPhone must have at most 20 characters")
		String emergencyContactPhone,

		String notes
) {
}
