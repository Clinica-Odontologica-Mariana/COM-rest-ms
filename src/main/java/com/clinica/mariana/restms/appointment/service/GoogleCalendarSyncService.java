package com.clinica.mariana.restms.appointment.service;

import com.clinica.mariana.restms.appointment.entity.AppointmentEntity;
import com.clinica.mariana.restms.appointment.entity.CalendarSyncStatusEntity;
import com.clinica.mariana.restms.appointment.repository.AppointmentRepository;
import com.clinica.mariana.restms.appointment.repository.CalendarSyncStatusRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class GoogleCalendarSyncService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCalendarSyncService.class);

	private final AppointmentRepository appointmentRepository;
	private final CalendarSyncStatusRepository calendarSyncStatusRepository;
	private final GoogleCalendarService googleCalendarService;
	private final EntityManager entityManager;

	public GoogleCalendarSyncService(AppointmentRepository appointmentRepository,
			CalendarSyncStatusRepository calendarSyncStatusRepository, GoogleCalendarService googleCalendarService,
			EntityManager entityManager) {
		this.appointmentRepository = appointmentRepository;
		this.calendarSyncStatusRepository = calendarSyncStatusRepository;
		this.googleCalendarService = googleCalendarService;
		this.entityManager = entityManager;
	}

	@Async
	@Transactional
	public void syncOnCreate(UUID appointmentId) {
		AppointmentEntity entity = appointmentRepository.findById(appointmentId).orElse(null);
		if (entity == null) {
			return;
		}

		try {
			String summary = buildSummary(entity.getPatientId());
			String eventId = googleCalendarService.createEvent(summary, entity.getNotes(), entity.getStartDatetime(),
					entity.getEndDatetime());

			entity.setExternalCalendarEventId(eventId);
			entity.setCalendarSyncStatus(findSyncStatus("SYNCED"));
			entity.setLastSyncedAt(OffsetDateTime.now());
		} catch (Exception e) {
			LOGGER.warn("Failed to sync appointment {} to Google Calendar on create", appointmentId, e);
			entity.setCalendarSyncStatus(findSyncStatus("FAILED"));
		}

		appointmentRepository.save(entity);
	}

	@Async
	@Transactional
	public void syncOnUpdate(UUID appointmentId, String notes, OffsetDateTime start, OffsetDateTime end) {
		AppointmentEntity entity = appointmentRepository.findById(appointmentId).orElse(null);
		if (entity == null || entity.getExternalCalendarEventId() == null) {
			return;
		}

		try {
			String summary = buildSummary(entity.getPatientId());
			googleCalendarService.updateEvent(entity.getExternalCalendarEventId(), summary, notes, start, end);
			entity.setCalendarSyncStatus(findSyncStatus("SYNCED"));
			entity.setLastSyncedAt(OffsetDateTime.now());
		} catch (Exception e) {
			LOGGER.warn("Failed to sync appointment {} to Google Calendar on update", appointmentId, e);
			entity.setCalendarSyncStatus(findSyncStatus("FAILED"));
		}

		appointmentRepository.save(entity);
	}

	@Async
	@Transactional
	public void syncOnDelete(UUID appointmentId) {
		AppointmentEntity entity = appointmentRepository.findById(appointmentId).orElse(null);
		if (entity == null || entity.getExternalCalendarEventId() == null) {
			return;
		}

		try {
			googleCalendarService.deleteEvent(entity.getExternalCalendarEventId());
			entity.setCalendarSyncStatus(findSyncStatus("NOT_SYNCED"));
			entity.setExternalCalendarEventId(null);
		} catch (Exception e) {
			LOGGER.warn("Failed to sync appointment {} to Google Calendar on delete", appointmentId, e);
			entity.setCalendarSyncStatus(findSyncStatus("FAILED"));
		}

		appointmentRepository.save(entity);
	}

	private CalendarSyncStatusEntity findSyncStatus(String code) {
		return calendarSyncStatusRepository.findByCode(code)
				.orElseThrow(() -> new IllegalStateException("Calendar sync status not configured: " + code));
	}

	private String buildSummary(UUID patientId) {
		String patientName = fetchPatientName(patientId);
		return patientName != null ? "Consulta - " + patientName : "Consulta";
	}

	private String fetchPatientName(UUID patientId) {
		if (patientId == null) {
			return null;
		}
		try {
			return (String) entityManager.createNativeQuery("SELECT full_name FROM patient WHERE id = :id")
					.setParameter("id", patientId).getSingleResult();
		} catch (Exception e) {
			LOGGER.warn("Could not fetch patient name for id {}", patientId, e);
			return null;
		}
	}
}
