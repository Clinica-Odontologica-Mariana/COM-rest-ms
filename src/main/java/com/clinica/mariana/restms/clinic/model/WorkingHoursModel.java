package com.clinica.mariana.restms.clinic.model;

import java.time.LocalTime;
import java.util.UUID;

public record WorkingHoursModel(UUID id, UUID clinicId, int dayOfWeek, LocalTime startTime, LocalTime endTime) {

	public WorkingHoursModel {
		if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {

			throw new IllegalArgumentException("endTime must be after startTime");
		}
	}

	public static WorkingHoursModel create(UUID clinicId, int dayOfWeek, LocalTime startTime, LocalTime endTime) {
		return new WorkingHoursModel(null, clinicId, dayOfWeek, startTime, endTime);
	}
}
