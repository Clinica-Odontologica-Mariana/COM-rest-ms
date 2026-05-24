package com.clinica.mariana.restms.appointment.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class GoogleCalendarConfig {

	@Value("${google.calendar.client-id}")
	private String clientId;

	@Value("${google.calendar.client-secret}")
	private String clientSecret;

	@Value("${google.calendar.refresh-token}")
	private String refreshToken;

	@Bean
	public Calendar googleCalendar() throws GeneralSecurityException, IOException {
		UserCredentials credentials = UserCredentials.newBuilder().setClientId(clientId).setClientSecret(clientSecret)
				.setRefreshToken(refreshToken).build();

		return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(),
				new HttpCredentialsAdapter(credentials)).setApplicationName("Clinica Mariana").build();
	}
}
