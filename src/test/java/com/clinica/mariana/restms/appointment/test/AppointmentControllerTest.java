package com.clinica.mariana.restms.appointment.test;

import com.clinica.mariana.restms.appointment.dto.AppointmentCreateDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentUpdateDto;
import com.clinica.mariana.restms.appointment.repository.AppointmentStatusRepository;
import com.clinica.mariana.restms.appointment.service.AppointmentService;
import com.clinica.mariana.restms.appointment.service.GoogleCalendarService;
import com.clinica.mariana.restms.appointment.service.GoogleCalendarSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class AppointmentControllerTest {

	private static final String SCHEDULED = "SCHEDULED";
	private static final String CONFIRMED = "CONFIRMED";
	private static final String GOOGLE_EVENT_INTEGRATION_TEST = "google-event-integration-test";
	private static final String CONSULTA_DE_ROTINA = "Consulta de rotina";
	private static final String CONSULTA_CONFIRMADA = "Consulta confirmada";
	private static final UUID PATIENT_ID = UUID.fromString("b1fbe6a7-477a-4db7-8e4c-c0730af0d281");
	private static final UUID CLINIC_ID = UUID.fromString("2525ad1f-0bdf-4d1c-b475-b6cf7f527d91");
	private static final UUID PROFESSIONAL_ID = UUID.fromString("65a2cd16-e6e4-4e93-9506-a11a4b20878b");
	private static final UUID USER_ID = UUID.fromString("17ed8803-a2af-4b9a-9657-366e676267d4");
	private static final UUID SPECIALTY_ID = UUID.fromString("b1d74a72-c661-43b7-9adb-cd6b9d3a40c2");

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private AppointmentStatusRepository appointmentStatusRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockitoBean
	private GoogleCalendarService googleCalendarService;

	@MockitoBean
	private GoogleCalendarSyncService googleCalendarSyncService;

	@BeforeEach
	void seedReferenceData() {
		jdbcTemplate.execute("create table if not exists app_user (id uuid primary key)");
		jdbcTemplate.execute("alter table app_user add column if not exists full_name varchar(150)");
		jdbcTemplate.update(
				"merge into clinic (id, name, phone, timezone, working_hours_json, active) key(id) values (?, ?, ?, ?, ?, ?)",
				CLINIC_ID, "Clínica Agenda", "61999999999", "America/Sao_Paulo", "[]", true);
		jdbcTemplate.update(
				"merge into patient (id, full_name, cpf, phone, birth_date, active) key(id) values (?, ?, ?, ?, ?, ?)",
				PATIENT_ID, "Paciente Agenda", "12345678901", "61988888888", LocalDate.of(1995, 1, 1), true);
		jdbcTemplate.update("merge into app_user (id, full_name) key(id) values (?, ?)", USER_ID,
				"Profissional Agenda");
		jdbcTemplate.update(
				"merge into professional (id, user_id, clinic_id, specialty_id, license_number, active) key(id) values (?, ?, ?, ?, ?, ?)",
				PROFESSIONAL_ID, USER_ID, CLINIC_ID, SPECIALTY_ID, "CRO-AGENDA-001", true);
	}

	@Test
	void shouldRunAppointmentCrudFlow() throws IOException {
		when(googleCalendarService.createEvent(any(), any(), any(), any())).thenReturn(GOOGLE_EVENT_INTEGRATION_TEST);

		UUID scheduledStatusId = appointmentStatusRepository.findByCode(SCHEDULED).orElseThrow().getId();

		LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
		LocalDateTime end = start.plusHours(1);

		AppointmentDto created = appointmentService.create(new AppointmentCreateDto(PATIENT_ID, CLINIC_ID, null,
				PROFESSIONAL_ID, scheduledStatusId, start, end, CONSULTA_DE_ROTINA, true));

		assertThat(created.id()).isNotNull();
		assertThat(created.statusCode()).isEqualTo(SCHEDULED);

		var all = appointmentService.findAll(Pageable.unpaged()).stream().toList();
		assertThat(all).anyMatch(a -> a.id().equals(created.id()));

		OffsetDateTime periodStart = start.minusHours(1).atOffset(ZoneOffset.UTC);
		OffsetDateTime periodEnd = end.plusHours(1).atOffset(ZoneOffset.UTC);
		var byPeriod = appointmentService.findByPeriod(periodStart, periodEnd, Pageable.unpaged()).stream().toList();
		assertThat(byPeriod).anyMatch(a -> a.id().equals(created.id()));

		UUID confirmedStatusId = appointmentStatusRepository.findByCode(CONFIRMED).orElseThrow().getId();

		AppointmentDto updated = appointmentService.update(created.id(), new AppointmentUpdateDto(confirmedStatusId,
				start.plusMinutes(15), end.plusMinutes(15), CONSULTA_CONFIRMADA, true));

		assertThat(updated.statusCode()).isEqualTo(CONFIRMED);
		assertThat(updated.notes()).isEqualTo(CONSULTA_CONFIRMADA);

		appointmentService.delete(created.id());

		var afterDelete = appointmentService.findAll(Pageable.unpaged()).stream().toList();
		assertThat(afterDelete).noneMatch(a -> a.id().equals(created.id()));
	}
}
