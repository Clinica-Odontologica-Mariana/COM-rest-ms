package com.clinica.mariana.restms.clinic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record WorkingHoursUpdateDto(

        @Min(value = 0, message = "dayOfWeek must be between 0 (Sunday) and 6 (Saturday)")
        @Max(value = 6, message = "dayOfWeek must be between 0 (Sunday) and 6 (Saturday)")
        int dayOfWeek,

        @NotNull(message = "startTime is required")
        LocalTime startTime,

        @NotNull(message = "endTime is required")
        LocalTime endTime
) {
}