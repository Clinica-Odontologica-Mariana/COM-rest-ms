package com.clinica.mariana.restms.certificate.test;

import com.clinica.mariana.restms.certificate.entity.CertificateEntity;
import com.clinica.mariana.restms.certificate.repository.CertificateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Certificate featured API")
class CertificateControllerTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final String BASE = CONTEXT_PATH + "/certificates";
	private static final UUID RANDOM_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CertificateRepository certificateRepository;

	private UUID certificateId;

	@BeforeEach
	void seedCertificate() {
		certificateRepository.deleteAll();
		CertificateEntity entity = new CertificateEntity();
		entity.setTitle("Certificado de teste");
		entity.setCertificateType("ATTENDANCE");
		entity.setIssuedAt(OffsetDateTime.now());
		entity.setActive(true);
		entity.setFeatured(false);
		certificateId = certificateRepository.save(entity).getId();
	}

	@Test
	@DisplayName("PATCH /certificates/{id}/featured as ADMIN returns 200")
	void setFeatured_asAdmin_returns200() throws Exception {
		mockMvc.perform(patch(BASE + "/" + certificateId + "/featured").contextPath(CONTEXT_PATH)
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON).content("{\"featured\":true}")).andExpect(status().isOk());
	}

	@Test
	@DisplayName("PATCH /certificates/{id}/featured as DOCTOR returns 403")
	void setFeatured_asDoctor_returns403() throws Exception {
		mockMvc.perform(patch(BASE + "/" + RANDOM_ID + "/featured").contextPath(CONTEXT_PATH)
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DOCTOR")))
				.contentType(MediaType.APPLICATION_JSON).content("{\"featured\":true}"))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("PATCH /certificates/{id}/featured unauthenticated returns 401")
	void setFeatured_unauthenticated_returns401() throws Exception {
		mockMvc.perform(patch(BASE + "/" + RANDOM_ID + "/featured").contextPath(CONTEXT_PATH)
				.contentType(MediaType.APPLICATION_JSON).content("{\"featured\":true}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("GET /certificates/featured unauthenticated returns 200")
	void getFeatured_unauthenticated_returns200() throws Exception {
		mockMvc.perform(get(BASE + "/featured").contextPath(CONTEXT_PATH)).andExpect(status().isOk());
	}
}
