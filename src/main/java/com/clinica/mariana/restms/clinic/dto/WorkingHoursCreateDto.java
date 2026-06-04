package com.clinica.mariana.restms.clinic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalTime;
import java.util.UUID;

public record WorkingHoursCreateDto(@NotNull(message = "clinicId is required") UUID clinicId,

		@NotNull(message = "dayOfWeek is required") @Min(value = 0, message = "dayOfWeek must be between 0 and 6") @Max(value = 6, message = "dayOfWeek must be between 0 and 6") Integer dayOfWeek,

		@NotNull(message = "startTime is required") LocalTime startTime,
		@NotNull(message = "endTime is required") LocalTime endTime) {
	@AssertTrue(message = "endTime must be after startTime")
	public boolean isTimeRangeValid() {
		if (startTime == null || endTime == null) {
			return true;
		}
		return endTime.isAfter(startTime);
	}
}
