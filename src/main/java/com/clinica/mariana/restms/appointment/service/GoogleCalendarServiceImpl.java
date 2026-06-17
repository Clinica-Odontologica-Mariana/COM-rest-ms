package com.clinica.mariana.restms.appointment.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

	private final Calendar calendar;

	@Value("${google.calendar.calendar-id:primary}")
	private String calendarId;

	@Value("${google.calendar.timezone:America/Sao_Paulo}")
	private String calendarTimezone;

	public GoogleCalendarServiceImpl(Calendar calendar) {
		this.calendar = calendar;
	}

	@Override
	public String createEvent(String summary, String description, OffsetDateTime start, OffsetDateTime end)
			throws IOException {
		Event event = buildEvent(summary, description, start, end);
		Event created = calendar.events().insert(calendarId, event).execute();
		return created.getId();
	}

	@Override
	public void updateEvent(String eventId, String summary, String description, OffsetDateTime start,
			OffsetDateTime end) throws IOException {
		Event event = buildEvent(summary, description, start, end);
		calendar.events().update(calendarId, eventId, event).execute();
	}

	@Override
	public void deleteEvent(String eventId) throws IOException {
		calendar.events().delete(calendarId, eventId).execute();
	}

	private Event buildEvent(String summary, String description, OffsetDateTime start, OffsetDateTime end) {
		ZoneId zone = ZoneId.of(calendarTimezone);

		// The stored OffsetDateTime uses UTC offset but represents local clinic time.
		// Re-interpret the local time components as the clinic's timezone before
		// sending
		// to Google Calendar, so the event appears at the correct local hour.
		ZonedDateTime startZoned = start.toLocalDateTime().atZone(zone);
		ZonedDateTime endZoned = end.toLocalDateTime().atZone(zone);

		EventDateTime startDt = new EventDateTime().setDateTime(new DateTime(startZoned.toInstant().toEpochMilli()))
				.setTimeZone(calendarTimezone);

		EventDateTime endDt = new EventDateTime().setDateTime(new DateTime(endZoned.toInstant().toEpochMilli()))
				.setTimeZone(calendarTimezone);

		return new Event().setSummary(summary).setDescription(description).setStart(startDt).setEnd(endDt);
	}
}
