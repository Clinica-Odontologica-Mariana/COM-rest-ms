package com.clinica.mariana.restms.appointment.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentCreateDto(

		@NotNull(message = "patientId is required")
		UUID patientId,

		@NotNull(message = "clinicId is required")
		UUID clinicId,

		UUID workplaceId,

		@NotNull(message = "professionalId is required")
		UUID professionalId,

		@NotNull(message = "statusId is required")
		UUID statusId,

		@NotNull(message = "startDatetime is required")
		OffsetDateTime startDatetime,

		@NotNull(message = "endDatetime is required")
		OffsetDateTime endDatetime,

		String notes,

		boolean blocksSchedule
) {
}
