package com.clinica.mariana.restms.appointment.entity;

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

	@Column(name = "created_by_user_id")
	private UUID createdByUserId;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getPatientId() {
		return patientId;
	}

	public void setPatientId(UUID patientId) {
		this.patientId = patientId;
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public void setClinicId(UUID clinicId) {
		this.clinicId = clinicId;
	}

	public UUID getWorkplaceId() {
		return workplaceId;
	}

	public void setWorkplaceId(UUID workplaceId) {
		this.workplaceId = workplaceId;
	}

	public UUID getProfessionalId() {
		return professionalId;
	}

	public void setProfessionalId(UUID professionalId) {
		this.professionalId = professionalId;
	}

	public AppointmentStatusEntity getStatus() {
		return status;
	}

	public void setStatus(AppointmentStatusEntity status) {
		this.status = status;
	}

	public CalendarProviderEntity getCalendarProvider() {
		return calendarProvider;
	}

	public void setCalendarProvider(CalendarProviderEntity calendarProvider) {
		this.calendarProvider = calendarProvider;
	}

	public CalendarSyncStatusEntity getCalendarSyncStatus() {
		return calendarSyncStatus;
	}

	public void setCalendarSyncStatus(CalendarSyncStatusEntity calendarSyncStatus) {
		this.calendarSyncStatus = calendarSyncStatus;
	}

	public String getExternalCalendarEventId() {
		return externalCalendarEventId;
	}

	public void setExternalCalendarEventId(String externalCalendarEventId) {
		this.externalCalendarEventId = externalCalendarEventId;
	}

	public OffsetDateTime getLastSyncedAt() {
		return lastSyncedAt;
	}

	public void setLastSyncedAt(OffsetDateTime lastSyncedAt) {
		this.lastSyncedAt = lastSyncedAt;
	}

	public boolean isBlocksSchedule() {
		return blocksSchedule;
	}

	public void setBlocksSchedule(boolean blocksSchedule) {
		this.blocksSchedule = blocksSchedule;
	}

	public OffsetDateTime getStartDatetime() {
		return startDatetime;
	}

	public void setStartDatetime(OffsetDateTime startDatetime) {
		this.startDatetime = startDatetime;
	}

	public OffsetDateTime getEndDatetime() {
		return endDatetime;
	}

	public void setEndDatetime(OffsetDateTime endDatetime) {
		this.endDatetime = endDatetime;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getCancellationReason() {
		return cancellationReason;
	}

	public void setCancellationReason(String cancellationReason) {
		this.cancellationReason = cancellationReason;
	}

	public OffsetDateTime getCancelledAt() {
		return cancelledAt;
	}

	public void setCancelledAt(OffsetDateTime cancelledAt) {
		this.cancelledAt = cancelledAt;
	}

	public UUID getCancelledByUserId() {
		return cancelledByUserId;
	}

	public void setCancelledByUserId(UUID cancelledByUserId) {
		this.cancelledByUserId = cancelledByUserId;
	}

	public UUID getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(UUID createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
