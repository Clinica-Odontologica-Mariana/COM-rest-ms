package com.clinica.mariana.restms.appointment.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentUpdateDto(

		@NotNull(message = "statusId is required") UUID statusId,

		UUID procedureId,

		@NotNull(message = "startDatetime is required") LocalDateTime startDatetime,

		@NotNull(message = "endDatetime is required") LocalDateTime endDatetime,

		String notes,

		boolean blocksSchedule) {
}
