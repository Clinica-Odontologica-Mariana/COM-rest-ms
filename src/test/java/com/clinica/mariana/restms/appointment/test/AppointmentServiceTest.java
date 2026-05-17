package com.clinica.mariana.restms.appointment.test;

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
import com.clinica.mariana.restms.appointment.service.AppointmentService;
import com.clinica.mariana.restms.appointment.service.GoogleCalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

	@Mock
	private AppointmentRepository appointmentRepository;

	@Mock
	private AppointmentStatusRepository appointmentStatusRepository;

	@Mock
	private CalendarProviderRepository calendarProviderRepository;

	@Mock
	private CalendarSyncStatusRepository calendarSyncStatusRepository;

	@Mock
	private GoogleCalendarService googleCalendarService;

	@InjectMocks
	private AppointmentService appointmentService;

	private AppointmentStatusEntity scheduledStatus;
	private AppointmentStatusEntity cancelledStatus;
	private CalendarProviderEntity googleProvider;
	private CalendarSyncStatusEntity pendingSync;
	private CalendarSyncStatusEntity syncedSync;
	private CalendarSyncStatusEntity failedSync;
	private CalendarSyncStatusEntity notSyncedSync;

	@BeforeEach
	void setUp() {
		scheduledStatus = buildStatus(UUID.randomUUID(), "SCHEDULED", "Agendado");
		cancelledStatus = buildStatus(UUID.randomUUID(), "CANCELLED", "Cancelado");

		googleProvider = new CalendarProviderEntity();
		googleProvider.setId(UUID.randomUUID());
		googleProvider.setCode("GOOGLE");

		pendingSync = buildSyncStatus(UUID.randomUUID(), "PENDING");
		syncedSync = buildSyncStatus(UUID.randomUUID(), "SYNCED");
		failedSync = buildSyncStatus(UUID.randomUUID(), "FAILED");
		notSyncedSync = buildSyncStatus(UUID.randomUUID(), "NOT_SYNCED");
	}

	@Test
	void shouldCreateAppointmentWithSuccessfulGoogleCalendarSync() throws IOException {
		AppointmentCreateDto request = buildCreateDto();

		when(appointmentStatusRepository.findById(request.statusId())).thenReturn(Optional.of(scheduledStatus));
		when(calendarProviderRepository.findByCode("GOOGLE")).thenReturn(Optional.of(googleProvider));
		when(calendarSyncStatusRepository.findByCode("PENDING")).thenReturn(Optional.of(pendingSync));
		when(calendarSyncStatusRepository.findByCode("SYNCED")).thenReturn(Optional.of(syncedSync));
		when(googleCalendarService.createEvent(any(), any(), any(), any())).thenReturn("google-event-id-123");
		when(appointmentRepository.save(any())).thenAnswer(inv -> {
			AppointmentEntity e = inv.getArgument(0);
			if (e.getId() == null) e.setId(UUID.randomUUID());
			if (e.getCreatedAt() == null) e.setCreatedAt(OffsetDateTime.now());
			if (e.getUpdatedAt() == null) e.setUpdatedAt(OffsetDateTime.now());
			return e;
		});

		AppointmentDto result = appointmentService.create(request);

		assertThat(result.id()).isNotNull();
		assertThat(result.externalCalendarEventId()).isEqualTo("google-event-id-123");
		assertThat(result.calendarSyncStatusCode()).isEqualTo("SYNCED");
		assertThat(result.statusCode()).isEqualTo("SCHEDULED");
		verify(googleCalendarService).createEvent(eq("Consulta"), any(), any(), any());
	}

	@Test
	void shouldCreateAppointmentWithFailedGoogleCalendarSync() throws IOException {
		AppointmentCreateDto request = buildCreateDto();

		when(appointmentStatusRepository.findById(request.statusId())).thenReturn(Optional.of(scheduledStatus));
		when(calendarProviderRepository.findByCode("GOOGLE")).thenReturn(Optional.of(googleProvider));
		when(calendarSyncStatusRepository.findByCode("PENDING")).thenReturn(Optional.of(pendingSync));
		when(calendarSyncStatusRepository.findByCode("FAILED")).thenReturn(Optional.of(failedSync));
		when(googleCalendarService.createEvent(any(), any(), any(), any()))
				.thenThrow(new IOException("Google Calendar unavailable"));
		when(appointmentRepository.save(any())).thenAnswer(inv -> {
			AppointmentEntity e = inv.getArgument(0);
			if (e.getId() == null) e.setId(UUID.randomUUID());
			if (e.getCreatedAt() == null) e.setCreatedAt(OffsetDateTime.now());
			if (e.getUpdatedAt() == null) e.setUpdatedAt(OffsetDateTime.now());
			return e;
		});

		AppointmentDto result = appointmentService.create(request);

		assertThat(result.calendarSyncStatusCode()).isEqualTo("FAILED");
		assertThat(result.externalCalendarEventId()).isNull();
	}

	@Test
	void shouldFindAllActiveAppointments() {
		AppointmentEntity entity = buildEntity(UUID.randomUUID());

		when(appointmentRepository.findAllByCancelledAtIsNullOrderByStartDatetimeAsc())
				.thenReturn(List.of(entity));

		List<AppointmentDto> result = appointmentService.findAll();

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().id()).isEqualTo(entity.getId());
	}

	@Test
	void shouldFindAppointmentsByPeriod() {
		OffsetDateTime start = OffsetDateTime.now();
		OffsetDateTime end = start.plusDays(7);
		AppointmentEntity entity = buildEntity(UUID.randomUUID());

		when(appointmentRepository
				.findByCancelledAtIsNullAndStartDatetimeBetweenOrderByStartDatetimeAsc(start, end))
				.thenReturn(List.of(entity));

		List<AppointmentDto> result = appointmentService.findByPeriod(start, end);

		assertThat(result).hasSize(1);
	}

	@Test
	void shouldUpdateAppointmentAndSyncToGoogleCalendar() throws IOException {
		UUID id = UUID.randomUUID();
		AppointmentEntity entity = buildEntity(id);
		entity.setExternalCalendarEventId("existing-event-id");
		entity.setCalendarSyncStatus(syncedSync);

		AppointmentUpdateDto request = new AppointmentUpdateDto(
				scheduledStatus.getId(),
				OffsetDateTime.now().plusDays(1),
				OffsetDateTime.now().plusDays(1).plusHours(1),
				"Updated notes",
				true
		);

		when(appointmentRepository.findById(id)).thenReturn(Optional.of(entity));
		when(appointmentStatusRepository.findById(request.statusId())).thenReturn(Optional.of(scheduledStatus));
		when(calendarSyncStatusRepository.findByCode("SYNCED")).thenReturn(Optional.of(syncedSync));
		when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		AppointmentDto result = appointmentService.update(id, request);

		assertThat(result.notes()).isEqualTo("Updated notes");
		verify(googleCalendarService).updateEvent(
				eq("existing-event-id"), eq("Consulta"), any(), any(), any());
	}

	@Test
	void shouldUpdateAppointmentWithoutGoogleCalendarSyncWhenNoEventId() throws IOException {
		UUID id = UUID.randomUUID();
		AppointmentEntity entity = buildEntity(id);
		entity.setExternalCalendarEventId(null);

		AppointmentUpdateDto request = new AppointmentUpdateDto(
				scheduledStatus.getId(),
				OffsetDateTime.now().plusDays(1),
				OffsetDateTime.now().plusDays(1).plusHours(1),
				"Updated notes",
				true
		);

		when(appointmentRepository.findById(id)).thenReturn(Optional.of(entity));
		when(appointmentStatusRepository.findById(request.statusId())).thenReturn(Optional.of(scheduledStatus));
		when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		appointmentService.update(id, request);

		verify(googleCalendarService, never()).updateEvent(any(), any(), any(), any(), any());
	}

	@Test
	void shouldThrowNotFoundWhenUpdatingNonExistentAppointment() {
		UUID id = UUID.randomUUID();
		AppointmentUpdateDto request = new AppointmentUpdateDto(
				scheduledStatus.getId(),
				OffsetDateTime.now(),
				OffsetDateTime.now().plusHours(1),
				null,
				true
		);

		when(appointmentRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> appointmentService.update(id, request))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Appointment not found");
	}

	@Test
	void shouldCancelAppointmentAndDeleteFromGoogleCalendar() throws IOException {
		UUID id = UUID.randomUUID();
		AppointmentEntity entity = buildEntity(id);
		entity.setExternalCalendarEventId("event-to-delete");
		entity.setCalendarSyncStatus(syncedSync);

		when(appointmentRepository.findById(id)).thenReturn(Optional.of(entity));
		when(appointmentStatusRepository.findByCode("CANCELLED")).thenReturn(Optional.of(cancelledStatus));
		when(calendarSyncStatusRepository.findByCode("NOT_SYNCED")).thenReturn(Optional.of(notSyncedSync));
		when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		appointmentService.delete(id);

		verify(googleCalendarService).deleteEvent("event-to-delete");
		assertThat(entity.getCancelledAt()).isNotNull();
		assertThat(entity.getStatus().getCode()).isEqualTo("CANCELLED");
		assertThat(entity.getExternalCalendarEventId()).isNull();
	}

	@Test
	void shouldSkipAlreadyCancelledAppointment() {
		UUID id = UUID.randomUUID();
		AppointmentEntity entity = buildEntity(id);
		entity.setCancelledAt(OffsetDateTime.now().minusDays(1));

		when(appointmentRepository.findById(id)).thenReturn(Optional.of(entity));

		appointmentService.delete(id);

		verify(appointmentRepository, never()).save(any());
	}

	@Test
	void shouldThrowNotFoundWhenCancellingNonExistentAppointment() {
		UUID id = UUID.randomUUID();

		when(appointmentRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> appointmentService.delete(id))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Appointment not found");
	}

	private AppointmentCreateDto buildCreateDto() {
		return new AppointmentCreateDto(
				UUID.randomUUID(),
				UUID.randomUUID(),
				null,
				UUID.randomUUID(),
				scheduledStatus.getId(),
				OffsetDateTime.now().plusDays(1),
				OffsetDateTime.now().plusDays(1).plusHours(1),
				"Consulta de rotina",
				true
		);
	}

	private AppointmentEntity buildEntity(UUID id) {
		AppointmentEntity entity = new AppointmentEntity();
		entity.setId(id);
		entity.setPatientId(UUID.randomUUID());
		entity.setClinicId(UUID.randomUUID());
		entity.setProfessionalId(UUID.randomUUID());
		entity.setStatus(scheduledStatus);
		entity.setCalendarSyncStatus(pendingSync);
		entity.setBlocksSchedule(true);
		entity.setStartDatetime(OffsetDateTime.now().plusDays(1));
		entity.setEndDatetime(OffsetDateTime.now().plusDays(1).plusHours(1));
		entity.setCreatedAt(OffsetDateTime.now());
		entity.setUpdatedAt(OffsetDateTime.now());
		return entity;
	}

	private AppointmentStatusEntity buildStatus(UUID id, String code, String name) {
		AppointmentStatusEntity s = new AppointmentStatusEntity();
		s.setId(id);
		s.setCode(code);
		s.setName(name);
		return s;
	}

	private CalendarSyncStatusEntity buildSyncStatus(UUID id, String code) {
		CalendarSyncStatusEntity s = new CalendarSyncStatusEntity();
		s.setId(id);
		s.setCode(code);
		s.setName(code);
		return s;
	}
}
