package com.clinica.mariana.restms.appointment.service;

import com.clinica.mariana.restms.appointment.dto.AppointmentCreateDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentUpdateDto;
import com.clinica.mariana.restms.appointment.entity.AppointmentEntity;
import com.clinica.mariana.restms.appointment.entity.AppointmentStatusEntity;
import com.clinica.mariana.restms.appointment.entity.CalendarProviderEntity;
import com.clinica.mariana.restms.appointment.entity.CalendarSyncStatusEntity;
import com.clinica.mariana.restms.appointment.repository.AppointmentRepository;
import com.clinica.mariana.restms.appointment.repository.AppointmentStatusRepository;
import com.clinica.mariana.restms.appointment.repository.CalendarProviderRepository;
import com.clinica.mariana.restms.appointment.repository.CalendarSyncStatusRepository;
import com.clinica.mariana.restms.common.exception.AppException;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final AppointmentStatusRepository appointmentStatusRepository;
	private final CalendarProviderRepository calendarProviderRepository;
	private final CalendarSyncStatusRepository calendarSyncStatusRepository;
	private final GoogleCalendarSyncService googleCalendarSyncService;
	private final EntityManager entityManager;

	public AppointmentService(AppointmentRepository appointmentRepository,
			AppointmentStatusRepository appointmentStatusRepository,
			CalendarProviderRepository calendarProviderRepository,
			CalendarSyncStatusRepository calendarSyncStatusRepository,
			GoogleCalendarSyncService googleCalendarSyncService, EntityManager entityManager) {
		this.appointmentRepository = appointmentRepository;
		this.appointmentStatusRepository = appointmentStatusRepository;
		this.calendarProviderRepository = calendarProviderRepository;
		this.calendarSyncStatusRepository = calendarSyncStatusRepository;
		this.googleCalendarSyncService = googleCalendarSyncService;
		this.entityManager = entityManager;
	}

	@Transactional
	public AppointmentDto create(AppointmentCreateDto request) {
		AppointmentStatusEntity status = request.statusId() != null
				? appointmentStatusRepository.findById(request.statusId())
						.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "APPOINTMENT_STATUS_NOT_FOUND",
								"Appointment status not found"))
				: appointmentStatusRepository.findByCode("SCHEDULED")
						.orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "CONFIGURATION_ERROR",
								"Default appointment status not configured"));

		CalendarProviderEntity googleProvider = calendarProviderRepository.findByCode("GOOGLE")
				.orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "CONFIGURATION_ERROR",
						"Google Calendar provider not configured"));

		CalendarSyncStatusEntity pendingSync = calendarSyncStatusRepository.findByCode("PENDING")
				.orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "CONFIGURATION_ERROR",
						"Calendar sync status not configured"));

		ensurePatientClinicLink(request.patientId(), request.clinicId());

		AppointmentEntity entity = new AppointmentEntity();
		entity.setPatientId(request.patientId());
		entity.setClinicId(request.clinicId());
		entity.setWorkplaceId(request.workplaceId());
		entity.setProfessionalId(request.professionalId());
		entity.setStatus(status);
		entity.setCalendarProvider(googleProvider);
		entity.setCalendarSyncStatus(pendingSync);
		entity.setBlocksSchedule(request.blocksSchedule());
		entity.setStartDatetime(toOffsetUtc(request.startDatetime()));
		entity.setEndDatetime(toOffsetUtc(request.endDatetime()));
		entity.setNotes(request.notes());

		AppointmentEntity saved = appointmentRepository.save(entity);
		UUID savedId = saved.getId();

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					googleCalendarSyncService.syncOnCreate(savedId);
				}
			});
		} else {
			googleCalendarSyncService.syncOnCreate(savedId);
		}

		return toDto(saved);
	}

	@Transactional(readOnly = true)
	public Page<AppointmentDto> findAll(Pageable pageable) {
		return appointmentRepository.findAllByCancelledAtIsNullOrderByStartDatetimeAsc(pageable).map(this::toDto);
	}

	@Transactional(readOnly = true)
	public Page<AppointmentDto> findByPeriod(OffsetDateTime start, OffsetDateTime end, Pageable pageable) {
		return appointmentRepository
				.findByCancelledAtIsNullAndStartDatetimeBetweenOrderByStartDatetimeAsc(start, end, pageable)
				.map(this::toDto);
	}

	@Transactional
	public AppointmentDto update(UUID id, AppointmentUpdateDto request) {
		AppointmentEntity entity = appointmentRepository.findById(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND", "Appointment not found"));

		AppointmentStatusEntity status = appointmentStatusRepository.findById(request.statusId())
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "APPOINTMENT_STATUS_NOT_FOUND",
						"Appointment status not found"));

		entity.setStatus(status);
		entity.setStartDatetime(toOffsetUtc(request.startDatetime()));
		entity.setEndDatetime(toOffsetUtc(request.endDatetime()));
		entity.setNotes(request.notes());
		entity.setBlocksSchedule(request.blocksSchedule());

		AppointmentEntity saved = appointmentRepository.save(entity);

		if (saved.getExternalCalendarEventId() != null) {
			String notes = request.notes();
			OffsetDateTime start = toOffsetUtc(request.startDatetime());
			OffsetDateTime end = toOffsetUtc(request.endDatetime());

			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
					@Override
					public void afterCommit() {
						googleCalendarSyncService.syncOnUpdate(saved.getId(), notes, start, end);
					}
				});
			} else {
				googleCalendarSyncService.syncOnUpdate(saved.getId(), notes, start, end);
			}
		}

		return toDto(saved);
	}

	@Transactional
	public void delete(UUID id) {
		AppointmentEntity entity = appointmentRepository.findById(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND", "Appointment not found"));

		if (entity.getCancelledAt() != null) {
			return;
		}

		AppointmentStatusEntity cancelledStatus = appointmentStatusRepository.findByCode("CANCELLED")
				.orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "CONFIGURATION_ERROR",
						"Appointment status not configured"));

		entity.setStatus(cancelledStatus);
		entity.setCancelledAt(OffsetDateTime.now());

		appointmentRepository.save(entity);

		UUID entityId = entity.getId();
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					googleCalendarSyncService.syncOnDelete(entityId);
				}
			});
		} else {
			googleCalendarSyncService.syncOnDelete(entityId);
		}
	}

	private void ensurePatientClinicLink(UUID patientId, UUID clinicId) {
		entityManager
				.createNativeQuery("INSERT INTO patient_clinic (patient_id, clinic_id, primary_clinic, active) "
						+ "VALUES (:patientId, :clinicId, false, true) ON CONFLICT DO NOTHING")
				.setParameter("patientId", patientId).setParameter("clinicId", clinicId).executeUpdate();
	}

	private OffsetDateTime toOffsetUtc(LocalDateTime ldt) {
		return ldt == null ? null : ldt.atOffset(ZoneOffset.UTC);
	}

	private String fetchPatientName(UUID patientId) {
		if (patientId == null) {
			return null;
		}
		try {
			return (String) entityManager.createNativeQuery("SELECT full_name FROM patient WHERE id = :id")
					.setParameter("id", patientId).getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	private String fetchProfessionalName(UUID professionalId) {
		if (professionalId == null) {
			return null;
		}
		try {
			return (String) entityManager.createNativeQuery(
					"SELECT u.full_name FROM app_user u JOIN professional p ON p.user_id = u.id WHERE p.id = :id")
					.setParameter("id", professionalId).getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	private AppointmentDto toDto(AppointmentEntity entity) {
		return new AppointmentDto(entity.getId(), entity.getPatientId(), fetchPatientName(entity.getPatientId()),
				entity.getClinicId(), entity.getWorkplaceId(), entity.getProfessionalId(),
				fetchProfessionalName(entity.getProfessionalId()),
				entity.getStatus() != null ? entity.getStatus().getId() : null,
				entity.getStatus() != null ? entity.getStatus().getCode() : null,
				entity.getStatus() != null ? entity.getStatus().getName() : null,
				entity.getCalendarSyncStatus() != null ? entity.getCalendarSyncStatus().getCode() : null,
				entity.getExternalCalendarEventId(), entity.getLastSyncedAt(), entity.isBlocksSchedule(),
				entity.getStartDatetime(), entity.getEndDatetime(), entity.getNotes(), entity.getCancellationReason(),
				entity.getCancelledAt(), entity.getCancelledByUserId(), entity.getCreatedByUserId(),
				entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
