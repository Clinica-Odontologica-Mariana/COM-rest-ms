package com.clinica.mariana.restms.patient.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PatientDto(
		UUID id,
		String fullName,
		String cpf,
		String phone,
		String email,
		LocalDate birthDate,
		boolean active
) {
}
