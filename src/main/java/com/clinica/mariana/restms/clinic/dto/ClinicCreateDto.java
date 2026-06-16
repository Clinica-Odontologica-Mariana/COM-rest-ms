package com.clinica.mariana.restms.clinic.dto;

import com.clinica.mariana.restms.address.dto.AddressCreateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ClinicCreateDto(UUID addressId,

		@NotBlank(message = "name is required") @Size(max = 150) String name,

		@NotBlank(message = "phone is required") @Size(max = 20) String phone,

		@Email(message = "email must be valid") @Size(max = 150) String email,

		@Size(max = 80) String timezone,

		@Size(max = 20) String whatsapp,

		@Size(max = 80) String instagram,

		@Pattern(regexp = "^(permanent|temporary)$", message = "inactiveType must be permanent or temporary") String inactiveType,

		LocalDate inactiveFrom,

		LocalDate inactiveTo,

		AddressCreateDto address,

		@Valid List<ClinicWorkingHoursSaveDto> workingHours) {
}
