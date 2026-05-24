package com.clinica.mariana.restms.security.config;

import com.clinica.mariana.restms.common.api.ApiError;
import com.clinica.mariana.restms.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		ApiResponse<Object> payload = ApiResponse.failure(new ApiError("UNAUTHORIZED", "Authentication is required",
				List.of(), Instant.now(), request.getRequestURI()));

		objectMapper.writeValue(response.getOutputStream(), payload);
	}
}
