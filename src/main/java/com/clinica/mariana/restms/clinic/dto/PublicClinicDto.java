package com.clinica.mariana.restms.clinic.dto;

import com.clinica.mariana.restms.address.dto.AddressDto;
import java.util.List;
import java.util.UUID;

public record PublicClinicDto(UUID id, String name, String phone, String email, String whatsapp, String instagram,
		String clinicPhotoUrl, AddressDto address, List<WorkingHoursDto> workingHours) {
}
