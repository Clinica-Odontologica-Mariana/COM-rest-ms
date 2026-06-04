package com.clinica.mariana.restms.appointment.test;

import com.clinica.mariana.restms.appointment.dto.AppointmentCreateDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentUpdateDto;
import com.clinica.mariana.restms.appointment.repository.AppointmentStatusRepository;
import com.clinica.mariana.restms.appointment.service.AppointmentService;
import com.clinica.mariana.restms.appointment.service.GoogleCalendarService;
import com.clinica.mariana.restms.appointment.service.GoogleCalendarSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.time.OffsetDateTime;
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

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private AppointmentStatusRepository appointmentStatusRepository;

	@MockitoBean
	private GoogleCalendarService googleCalendarService;

	@MockitoBean
	private GoogleCalendarSyncService googleCalendarSyncService;

	@Test
	void shouldRunAppointmentCrudFlow() throws IOException {
		when(googleCalendarService.createEvent(any(), any(), any(), any())).thenReturn(GOOGLE_EVENT_INTEGRATION_TEST);

		UUID scheduledStatusId = appointmentStatusRepository.findByCode(SCHEDULED).orElseThrow().getId();

		OffsetDateTime start = OffsetDateTime.now().plusDays(1).withNano(0);
		OffsetDateTime end = start.plusHours(1);

		AppointmentDto created = appointmentService.create(new AppointmentCreateDto(UUID.randomUUID(),
				UUID.randomUUID(), null, UUID.randomUUID(), scheduledStatusId, start, end, CONSULTA_DE_ROTINA, true));

		assertThat(created.id()).isNotNull();
		assertThat(created.statusCode()).isEqualTo(SCHEDULED);

		var all = appointmentService.findAll(Pageable.unpaged()).stream().toList();
		assertThat(all).anyMatch(a -> a.id().equals(created.id()));

		OffsetDateTime periodStart = start.minusHours(1);
		OffsetDateTime periodEnd = end.plusHours(1);
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
