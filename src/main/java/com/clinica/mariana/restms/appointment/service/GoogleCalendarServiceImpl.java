package com.clinica.mariana.restms.appointment.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;

@Service
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

	private final Calendar calendar;

	@Value("${google.calendar.calendar-id:primary}")
	private String calendarId;

	public GoogleCalendarServiceImpl(Calendar calendar) {
		this.calendar = calendar;
	}

	@Override
	public String createEvent(String summary, String description, OffsetDateTime start, OffsetDateTime end) throws IOException {
		Event event = buildEvent(summary, description, start, end);
		Event created = calendar.events().insert(calendarId, event).execute();
		return created.getId();
	}

	@Override
	public void updateEvent(String eventId, String summary, String description, OffsetDateTime start, OffsetDateTime end) throws IOException {
		Event event = buildEvent(summary, description, start, end);
		calendar.events().update(calendarId, eventId, event).execute();
	}

	@Override
	public void deleteEvent(String eventId) throws IOException {
		calendar.events().delete(calendarId, eventId).execute();
	}

	private Event buildEvent(String summary, String description, OffsetDateTime start, OffsetDateTime end) {
		EventDateTime startDt = new EventDateTime()
				.setDateTime(new DateTime(start.toInstant().toEpochMilli()))
				.setTimeZone("UTC");

		EventDateTime endDt = new EventDateTime()
				.setDateTime(new DateTime(end.toInstant().toEpochMilli()))
				.setTimeZone("UTC");

		return new Event()
				.setSummary(summary)
				.setDescription(description)
				.setStart(startDt)
				.setEnd(endDt);
	}
}
