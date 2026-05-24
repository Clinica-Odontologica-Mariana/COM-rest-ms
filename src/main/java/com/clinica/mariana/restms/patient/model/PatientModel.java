package com.clinica.mariana.restms.patient.model;

import java.time.LocalDate;
import java.util.UUID;

public record PatientModel(UUID id, UUID addressId, UUID createdByUserId, String fullName, String cpf, String phone,
		String email, LocalDate birthDate, String emergencyContactName, String emergencyContactPhone, String notes,
		boolean active) {
}
