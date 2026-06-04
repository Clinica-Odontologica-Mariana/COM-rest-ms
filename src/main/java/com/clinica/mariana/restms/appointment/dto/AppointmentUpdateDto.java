package com.clinica.mariana.restms.appointment.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentUpdateDto(

		@NotNull(message = "statusId is required") UUID statusId,

		@NotNull(message = "startDatetime is required") OffsetDateTime startDatetime,

		@NotNull(message = "endDatetime is required") OffsetDateTime endDatetime,

		String notes,

		boolean blocksSchedule) {
}
