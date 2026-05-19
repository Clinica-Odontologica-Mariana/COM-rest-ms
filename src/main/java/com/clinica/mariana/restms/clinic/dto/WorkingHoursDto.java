package com.clinica.mariana.restms.clinic.dto;

import java.time.LocalTime;
import java.util.UUID;

public record WorkingHoursDto(
        UUID id,
        UUID clinicId,
        int dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}