package com.clinica.mariana.restms.appointment.service;

import com.clinica.mariana.restms.appointment.dto.AppointmentCreateDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentUpdateDto;
import com.clinica.mariana.restms.appointment.entity.AppointmentEntity;
import com.clinica.mariana.restms.appointment.entity.AppointmentStatusEntity;
import com.clinica.mariana.restms.appointment.entity.CalendarProviderEntity;
import com.clinica.mariana.restms.appointment.entity.CalendarSyncStatusEntity;
import com.clinica.mariana.restms.appointment.model.AppointmentModel;
import com.clinica.mariana.restms.appointment.repository.AppointmentRepository;
import com.clinica.mariana.restms.appointment.repository.AppointmentStatusRepository;
import com.clinica.mariana.restms.appointment.repository.CalendarProviderRepository;
import com.clinica.mariana.restms.appointment.repository.CalendarSyncStatusRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final AppointmentStatusRepository appointmentStatusRepository;
	private final CalendarProviderRepository calendarProviderRepository;
	private final CalendarSyncStatusRepository calendarSyncStatusRepository;
	private final GoogleCalendarService googleCalendarService;

	public AppointmentService(
			AppointmentRepository appointmentRepository,
			AppointmentStatusRepository appointmentStatusRepository,
			CalendarProviderRepository calendarProviderRepository,
			CalendarSyncStatusRepository calendarSyncStatusRepository,
			GoogleCalendarService googleCalendarService) {
		this.appointmentRepository = appointmentRepository;
		this.appointmentStatusRepository = appointmentStatusRepository;
		this.calendarProviderRepository = calendarProviderRepository;
		this.calendarSyncStatusRepository = calendarSyncStatusRepository;
		this.googleCalendarService = googleCalendarService;
	}

	@Transactional
	public AppointmentDto create(AppointmentCreateDto request) {
		AppointmentStatusEntity status = appointmentStatusRepository.findById(request.statusId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment status not found"));

		CalendarProviderEntity googleProvider = calendarProviderRepository.findByCode("GOOGLE")
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Google Calendar provider not configured"));

		CalendarSyncStatusEntity pendingSync = calendarSyncStatusRepository.findByCode("PENDING")
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Calendar sync status not configured"));

		AppointmentEntity entity = new AppointmentEntity();
		entity.setPatientId(request.patientId());
		entity.setClinicId(request.clinicId());
		entity.setWorkplaceId(request.workplaceId());
		entity.setProfessionalId(request.professionalId());
		entity.setStatus(status);
		entity.setCalendarProvider(googleProvider);
		entity.setCalendarSyncStatus(pendingSync);
		entity.setBlocksSchedule(request.blocksSchedule());
		entity.setStartDatetime(request.startDatetime());
		entity.setEndDatetime(request.endDatetime());
		entity.setNotes(request.notes());

		AppointmentEntity saved = appointmentRepository.save(entity);

		try {
			String eventId = googleCalendarService.createEvent(
					"Consulta",
					request.notes(),
					request.startDatetime(),
					request.endDatetime()
			);

			CalendarSyncStatusEntity synced = calendarSyncStatusRepository.findByCode("SYNCED")
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Calendar sync status not configured"));

			saved.setExternalCalendarEventId(eventId);
			saved.setCalendarSyncStatus(synced);
			saved.setLastSyncedAt(OffsetDateTime.now());
		} catch (Exception e) {
			CalendarSyncStatusEntity failed = calendarSyncStatusRepository.findByCode("FAILED")
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Calendar sync status not configured"));

			saved.setCalendarSyncStatus(failed);
		}

		return toDto(toModel(appointmentRepository.save(saved)));
	}

	@Transactional(readOnly = true)
	public List<AppointmentDto> findAll() {
		return appointmentRepository.findAllByCancelledAtIsNullOrderByStartDatetimeAsc()
				.stream()
				.map(this::toModel)
				.map(this::toDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<AppointmentDto> findByPeriod(OffsetDateTime start, OffsetDateTime end) {
		return appointmentRepository
				.findByCancelledAtIsNullAndStartDatetimeBetweenOrderByStartDatetimeAsc(start, end)
				.stream()
				.map(this::toModel)
				.map(this::toDto)
				.toList();
	}

	@Transactional
	public AppointmentDto update(UUID id, AppointmentUpdateDto request) {
		AppointmentEntity entity = appointmentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

		AppointmentStatusEntity status = appointmentStatusRepository.findById(request.statusId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment status not found"));

		entity.setStatus(status);
		entity.setStartDatetime(request.startDatetime());
		entity.setEndDatetime(request.endDatetime());
		entity.setNotes(request.notes());
		entity.setBlocksSchedule(request.blocksSchedule());

		AppointmentEntity saved = appointmentRepository.save(entity);

		if (saved.getExternalCalendarEventId() != null) {
			try {
				googleCalendarService.updateEvent(
						saved.getExternalCalendarEventId(),
						"Consulta",
						request.notes(),
						request.startDatetime(),
						request.endDatetime()
				);

				CalendarSyncStatusEntity synced = calendarSyncStatusRepository.findByCode("SYNCED")
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Calendar sync status not configured"));

				saved.setCalendarSyncStatus(synced);
				saved.setLastSyncedAt(OffsetDateTime.now());
			} catch (Exception e) {
				CalendarSyncStatusEntity failed = calendarSyncStatusRepository.findByCode("FAILED")
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Calendar sync status not configured"));

				saved.setCalendarSyncStatus(failed);
			}

			appointmentRepository.save(saved);
		}

		return toDto(toModel(saved));
	}

	@Transactional
	public void delete(UUID id) {
		AppointmentEntity entity = appointmentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

		if (entity.getCancelledAt() != null) {
			return;
		}

		AppointmentStatusEntity cancelledStatus = appointmentStatusRepository.findByCode("CANCELLED")
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Appointment status not configured"));

		entity.setStatus(cancelledStatus);
		entity.setCancelledAt(OffsetDateTime.now());

		if (entity.getExternalCalendarEventId() != null) {
			try {
				googleCalendarService.deleteEvent(entity.getExternalCalendarEventId());

				CalendarSyncStatusEntity notSynced = calendarSyncStatusRepository.findByCode("NOT_SYNCED")
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Calendar sync status not configured"));

				entity.setCalendarSyncStatus(notSynced);
				entity.setExternalCalendarEventId(null);
			} catch (Exception e) {
				CalendarSyncStatusEntity failed = calendarSyncStatusRepository.findByCode("FAILED")
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Calendar sync status not configured"));

				entity.setCalendarSyncStatus(failed);
			}
		}

		appointmentRepository.save(entity);
	}

	private AppointmentModel toModel(AppointmentEntity entity) {
		return new AppointmentModel(
				entity.getId(),
				entity.getPatientId(),
				entity.getClinicId(),
				entity.getWorkplaceId(),
				entity.getProfessionalId(),
				entity.getStatus() != null ? entity.getStatus().getCode() : null,
				entity.getStatus() != null ? entity.getStatus().getName() : null,
				entity.getCalendarSyncStatus() != null ? entity.getCalendarSyncStatus().getCode() : null,
				entity.getExternalCalendarEventId(),
				entity.getLastSyncedAt(),
				entity.isBlocksSchedule(),
				entity.getStartDatetime(),
				entity.getEndDatetime(),
				entity.getNotes(),
				entity.getCancellationReason(),
				entity.getCancelledAt(),
				entity.getCancelledByUserId(),
				entity.getCreatedByUserId(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	private AppointmentDto toDto(AppointmentModel model) {
		return new AppointmentDto(
				model.id(),
				model.patientId(),
				model.clinicId(),
				model.workplaceId(),
				model.professionalId(),
				model.statusCode(),
				model.statusName(),
				model.calendarSyncStatusCode(),
				model.externalCalendarEventId(),
				model.lastSyncedAt(),
				model.blocksSchedule(),
				model.startDatetime(),
				model.endDatetime(),
				model.notes(),
				model.cancellationReason(),
				model.cancelledAt(),
				model.cancelledByUserId(),
				model.createdByUserId(),
				model.createdAt(),
				model.updatedAt()
		);
	}
}
