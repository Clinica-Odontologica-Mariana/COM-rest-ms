package com.clinica.mariana.restms.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientCreateDto(
		@NotBlank(message = "fullName is required")
		@Size(max = 150, message = "fullName must have at most 150 characters")
		String fullName,

		@NotBlank(message = "cpf is required")
		@Pattern(regexp = "^[0-9]{11}$", message = "cpf must contain exactly 11 digits")
		String cpf,

		@NotBlank(message = "phone is required")
		@Size(max = 20, message = "phone must have at most 20 characters")
		String phone,

		@Size(max = 150, message = "email must have at most 150 characters")
		String email,

		LocalDate birthDate
) {
}
