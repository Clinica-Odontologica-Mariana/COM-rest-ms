package com.clinica.mariana.restms.clinic.model;

import java.time.LocalTime;
import java.util.UUID;

public record WorkingHoursModel(
        UUID id,
        UUID clinicId,
        int dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
    public WorkingHoursModel {
        if (clinicId == null) throw new IllegalArgumentException("clinicId is required");
        if (dayOfWeek < 0 || dayOfWeek > 6)
            throw new IllegalArgumentException("dayOfWeek must be between 0 (Sunday) and 6 (Saturday)");
        if (startTime == null) throw new IllegalArgumentException("startTime is required");
        if (endTime == null) throw new IllegalArgumentException("endTime is required");
        if (!endTime.isAfter(startTime))
            throw new IllegalArgumentException("endTime must be after startTime");
    }

    public static WorkingHoursModel create(UUID clinicId, int dayOfWeek, LocalTime startTime, LocalTime endTime) {
        return new WorkingHoursModel(null, clinicId, dayOfWeek, startTime, endTime);
    }

    public WorkingHoursModel withId(UUID id) {
        return new WorkingHoursModel(id, clinicId, dayOfWeek, startTime, endTime);
    }
}