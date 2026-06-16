package com.clinica.mariana.restms.clinic.model;

import java.time.LocalTime;
import java.util.UUID;

public record ClinicStoredWorkingHours(UUID id, int dayOfWeek, LocalTime startTime, LocalTime endTime) {
}
