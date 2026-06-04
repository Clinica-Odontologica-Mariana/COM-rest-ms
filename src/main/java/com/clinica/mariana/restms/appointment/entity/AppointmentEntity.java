package com.clinica.mariana.restms.appointment.entity;

import lombok.Getter;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedBy;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@Setter
@Entity
@Table(name = "appointment")
public class AppointmentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "patient_id", nullable = false)
	private UUID patientId;

	@Column(name = "clinic_id", nullable = false)
	private UUID clinicId;

	@Column(name = "workplace_id")
	private UUID workplaceId;

	@Column(name = "professional_id", nullable = false)
	private UUID professionalId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "status_id", nullable = false)
	private AppointmentStatusEntity status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "calendar_provider_id")
	private CalendarProviderEntity calendarProvider;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "calendar_sync_status_id")
	private CalendarSyncStatusEntity calendarSyncStatus;

	@Column(name = "external_calendar_event_id", length = 255)
	private String externalCalendarEventId;

	@Column(name = "last_synced_at")
	private OffsetDateTime lastSyncedAt;

	@Column(name = "blocks_schedule", nullable = false)
	private boolean blocksSchedule = true;

	@Column(name = "start_datetime", nullable = false)
	private OffsetDateTime startDatetime;

	@Column(name = "end_datetime", nullable = false)
	private OffsetDateTime endDatetime;

	@Column(name = "notes")
	private String notes;

	@Column(name = "cancellation_reason")
	private String cancellationReason;

	@Column(name = "cancelled_at")
	private OffsetDateTime cancelledAt;

	@Column(name = "cancelled_by_user_id")
	private UUID cancelledByUserId;

	@CreatedBy
	@Column(name = "created_by_user_id", updatable = false)
	private UUID createdByUserId;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

}
