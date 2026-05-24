package com.clinica.mariana.restms.appointment.service;

import java.io.IOException;
import java.time.OffsetDateTime;

public interface GoogleCalendarService {

	String createEvent(String summary, String description, OffsetDateTime start, OffsetDateTime end) throws IOException;

	void updateEvent(String eventId, String summary, String description, OffsetDateTime start, OffsetDateTime end)
			throws IOException;

	void deleteEvent(String eventId) throws IOException;
}
