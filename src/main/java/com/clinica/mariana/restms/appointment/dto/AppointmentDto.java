package com.clinica.mariana.restms.appointment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentDto(UUID id, UUID patientId, String patientName, UUID clinicId, UUID workplaceId,
		UUID procedureId, UUID professionalId, String professionalName, UUID statusId, String statusCode, String statusName,
		String calendarSyncStatusCode, String externalCalendarEventId, OffsetDateTime lastSyncedAt,
		boolean blocksSchedule, OffsetDateTime startDatetime, OffsetDateTime endDatetime, String notes,
		String cancellationReason, OffsetDateTime cancelledAt, UUID cancelledByUserId, UUID createdByUserId,
		OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
