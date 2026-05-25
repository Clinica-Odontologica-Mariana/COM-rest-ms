package com.clinica.mariana.restms.security.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinica.mariana.restms.security.config.RestAccessDeniedHandler;
import com.clinica.mariana.restms.security.config.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

class SecurityHandlersTest {

	@Test
	void shouldWriteUnauthorizedEnvelope() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/patients");
		MockHttpServletResponse response = new MockHttpServletResponse();

		new RestAuthenticationEntryPoint().commence(request, response, new BadCredentialsException("bad token"));

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentAsString()).contains("\"success\":false");
		assertThat(response.getContentAsString()).contains("\"code\":\"UNAUTHORIZED\"");
	}

	@Test
	void shouldWriteForbiddenEnvelope() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/patients");
		MockHttpServletResponse response = new MockHttpServletResponse();

		new RestAccessDeniedHandler().handle(request, response, new AccessDeniedException("missing role"));

		assertThat(response.getStatus()).isEqualTo(403);
		assertThat(response.getContentAsString()).contains("\"success\":false");
		assertThat(response.getContentAsString()).contains("\"code\":\"FORBIDDEN\"");
	}
}
