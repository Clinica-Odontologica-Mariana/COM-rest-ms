package com.clinica.mariana.restms.appointment.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentCreateDto(

		@NotNull(message = "patientId is required") UUID patientId,

		@NotNull(message = "clinicId is required") UUID clinicId,

		UUID workplaceId,

		@NotNull(message = "professionalId is required") UUID professionalId,

		UUID statusId,

		@NotNull(message = "startDatetime is required") LocalDateTime startDatetime,

		@NotNull(message = "endDatetime is required") LocalDateTime endDatetime,

		String notes,

		boolean blocksSchedule) {
}
