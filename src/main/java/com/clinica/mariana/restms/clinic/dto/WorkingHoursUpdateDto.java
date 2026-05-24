package com.clinica.mariana.restms.clinic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

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
        @AssertTrue(message = "endTime must be after startTime")
        public boolean isEndTimeAfterStartTime() {
                if (startTime == null || endTime == null) return true;
                return endTime.isAfter(startTime);
        }
}