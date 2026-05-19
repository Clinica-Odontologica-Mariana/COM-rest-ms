package com.clinica.mariana.restms.security.config;

import com.clinica.mariana.restms.common.api.ApiError;
import com.clinica.mariana.restms.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException
	) throws IOException {
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		ApiResponse<Object> payload = ApiResponse.failure(new ApiError(
				"FORBIDDEN",
				"You do not have permission to access this resource",
				List.of(),
				Instant.now(),
				request.getRequestURI()
		));

		objectMapper.writeValue(response.getOutputStream(), payload);
	}
}
