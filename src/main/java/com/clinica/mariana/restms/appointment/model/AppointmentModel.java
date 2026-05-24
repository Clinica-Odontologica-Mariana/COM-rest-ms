package com.clinica.mariana.restms.appointment.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentModel(
		UUID id,
		UUID patientId,
		UUID clinicId,
		UUID workplaceId,
		UUID professionalId,
		String statusCode,
		String statusName,
		String calendarSyncStatusCode,
		String externalCalendarEventId,
		OffsetDateTime lastSyncedAt,
		boolean blocksSchedule,
		OffsetDateTime startDatetime,
		OffsetDateTime endDatetime,
		String notes,
		String cancellationReason,
		OffsetDateTime cancelledAt,
		UUID cancelledByUserId,
		UUID createdByUserId,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
