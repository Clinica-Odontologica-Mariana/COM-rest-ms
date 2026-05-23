package com.clinica.mariana.restms.common.web;

import com.clinica.mariana.restms.common.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void shouldMapAccessDeniedToForbiddenEnvelope() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/professionals");
		ServletWebRequest webRequest = new ServletWebRequest(request);

		var response = handler.handleAccessDenied(new AccessDeniedException("missing role"), webRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(response.getBody()).isNotNull();

		ApiResponse<Object> body = response.getBody();
		assertThat(body.success()).isFalse();
		assertThat(body.error()).isNotNull();
		assertThat(body.error().code()).isEqualTo("FORBIDDEN");
		assertThat(body.error().path()).isEqualTo("/api/v1/professionals");
	}
}
