package com.clinica.mariana.restms.clinic.dto;

import com.clinica.mariana.restms.address.dto.AddressDto;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ClinicDto(UUID id, UUID addressId, String name, String phone, String email, String timezone,
		String whatsapp, String instagram, UUID clinicPhotoFileId, String clinicPhotoUrl, String inactiveType,
		LocalDate inactiveFrom, LocalDate inactiveTo, boolean active, OffsetDateTime createdAt,
		OffsetDateTime updatedAt, AddressDto address, List<WorkingHoursDto> workingHours) {
}
