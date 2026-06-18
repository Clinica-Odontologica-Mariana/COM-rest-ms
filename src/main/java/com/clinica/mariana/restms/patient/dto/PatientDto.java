package com.clinica.mariana.restms.patient.dto;

import com.clinica.mariana.restms.address.dto.AddressDto;

import java.time.LocalDate;
import java.util.UUID;

public record PatientDto(UUID id, UUID addressId, AddressDto address, UUID createdByUserId, String fullName, String cpf,
		String phone, String email, LocalDate birthDate, String emergencyContactName, String emergencyContactPhone,
		String notes, boolean active) {
}
