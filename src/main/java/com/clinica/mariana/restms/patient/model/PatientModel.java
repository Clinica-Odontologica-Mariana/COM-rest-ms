package com.clinica.mariana.restms.patient.model;

import java.time.LocalDate;
import java.util.UUID;

public record PatientModel(
		UUID id,
		String fullName,
		String cpf,
		String phone,
		String email,
		LocalDate birthDate,
		boolean active
) {
}
