package com.clinica.mariana.restms.storedfile.integration;

import com.clinica.mariana.restms.patient.entity.PatientEntity;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import com.clinica.mariana.restms.storedfile.repository.OdontogramFileRepository;
import com.clinica.mariana.restms.storedfile.repository.StoredFileRepository;
import com.clinica.mariana.restms.storedfile.repository.UserProfilePhotoRepository;
import com.clinica.mariana.restms.storedfile.service.MinioStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Stored files integration")
class StoredFileControllerIntegrationTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final String USER_SUBJECT = "stored-file-user";
	private static final UUID ADMIN_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
	private static final UUID OWN_USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private StoredFileRepository storedFileRepository;

	@Autowired
	private UserProfilePhotoRepository userProfilePhotoRepository;

	@Autowired
	private OdontogramFileRepository odontogramFileRepository;

	@MockitoBean
	private MinioStorageService minioStorageService;

	private PatientEntity patient;

	@BeforeEach
	void setUp() {
		userProfilePhotoRepository.deleteAll();
		odontogramFileRepository.deleteAll();
		storedFileRepository.deleteAll();
		patientRepository.deleteAll();
		ensureAppUserTable();
		jdbcTemplate.update("delete from app_user");

		insertUser(ADMIN_USER_ID, "admin-subject", "admin@clinic.com");
		insertUser(OWN_USER_ID, USER_SUBJECT, "profile@clinic.com");
		patient = patientRepository.save(patient("12345678901"));

		when(minioStorageService.upload(any(), any(), any())).thenAnswer(
				invocation -> new MinioStorageService.UploadResult("test-bucket", invocation.getArgument(1), "etag"));
		when(minioStorageService.presignedDownloadUrl(any())).thenReturn(new MinioStorageService.PresignedObjectUrl(
				"http://localhost:9000/test-bucket/object?signature=test", OffsetDateTime.now().plusMinutes(5)));
	}

	@Test
	void shouldUploadOwnProfilePhotoAndReturnPresignedUrl() throws Exception {
		mockMvc.perform(multipart("/api/v1/users/me/profile-photo").file(profilePhoto()).contextPath(CONTEXT_PATH)
				.with(jwt().jwt(jwt -> jwt.subject(USER_SUBJECT)))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.userId", is(OWN_USER_ID.toString())))
				.andExpect(jsonPath("$.data.file.category", is("USER_PROFILE_PHOTO")));

		mockMvc.perform(get("/api/v1/users/me/profile-photo/download-url").contextPath(CONTEXT_PATH)
				.with(jwt().jwt(jwt -> jwt.subject(USER_SUBJECT)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.url", is("http://localhost:9000/test-bucket/object?signature=test")));
	}

	@Test
	void shouldRejectOtherUserProfilePhotoUploadWithoutAdminRole() throws Exception {
		mockMvc.perform(multipart("/api/v1/users/{userId}/profile-photo", OWN_USER_ID).file(profilePhoto())
				.contextPath(CONTEXT_PATH).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DOCTOR"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldUploadFindDownloadAndHardDeleteOdontogram() throws Exception {
		String response = mockMvc
				.perform(multipart("/api/v1/stored-files/odontograms/{patientId}", patient.getId())
						.file(odontogramPdf()).param("description", "Odontograma inicial").contextPath(CONTEXT_PATH)
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DOCTOR"))))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.data.patientId", is(patient.getId().toString())))
				.andExpect(jsonPath("$.data.file.category", is("ODONTOGRAM"))).andReturn().getResponse()
				.getContentAsString();

		String id = objectMapper.readTree(response).get("data").get("id").asText();
		mockMvc.perform(get("/api/v1/stored-files/odontograms/{id}", UUID.fromString(id)).contextPath(CONTEXT_PATH)
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DOCTOR")))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id", is(id)))
				.andExpect(jsonPath("$.data.description", is("Odontograma inicial")));
		mockMvc.perform(get("/api/v1/stored-files/odontograms/{id}/download-url", UUID.fromString(id))
				.contextPath(CONTEXT_PATH).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DOCTOR"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.url", is("http://localhost:9000/test-bucket/object?signature=test")));
		mockMvc.perform(delete("/api/v1/stored-files/odontograms/{id}", UUID.fromString(id)).contextPath(CONTEXT_PATH)
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DOCTOR")))).andExpect(status().isNoContent());
	}

	@Test
	void shouldReturn401WhenUploadingOdontogramWithoutJwt() throws Exception {
		mockMvc.perform(multipart("/api/v1/stored-files/odontograms/{patientId}", patient.getId()).file(odontogramPdf())
				.contextPath(CONTEXT_PATH)).andExpect(status().isUnauthorized());
	}

	@ParameterizedTest
	@ValueSource(strings = {"ROLE_ASSISTANT", "ROLE_PATIENT"})
	void shouldRejectOdontogramUploadForUnauthorizedRoles(String role) throws Exception {
		mockMvc.perform(multipart("/api/v1/stored-files/odontograms/{patientId}", patient.getId()).file(odontogramPdf())
				.contextPath(CONTEXT_PATH).with(jwt().authorities(new SimpleGrantedAuthority(role))))
				.andExpect(status().isForbidden());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidProfilePhotos")
	void shouldRejectInvalidOwnProfilePhoto(String scenario, MockMultipartFile file, HttpStatus expectedStatus)
			throws Exception {
		mockMvc.perform(multipart("/api/v1/users/me/profile-photo").file(file).contextPath(CONTEXT_PATH)
				.with(jwt().jwt(jwt -> jwt.subject(USER_SUBJECT)))).andExpect(status().is(expectedStatus.value()));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidOdontogramFiles")
	void shouldRejectInvalidOdontogramUpload(String scenario, MockMultipartFile file, HttpStatus expectedStatus)
			throws Exception {
		mockMvc.perform(multipart("/api/v1/stored-files/odontograms/{patientId}", patient.getId()).file(file)
				.contextPath(CONTEXT_PATH).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DOCTOR"))))
				.andExpect(status().is(expectedStatus.value()));
	}

	private MockMultipartFile profilePhoto() {
		return new MockMultipartFile("file", "profile.png", "image/png",
				new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
	}

	private MockMultipartFile odontogramPdf() {
		return new MockMultipartFile("file", "odontogram.pdf", "application/pdf",
				new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D});
	}

	static Stream<Arguments> invalidProfilePhotos() {
		return Stream.of(Arguments.of("empty profile photo",
				new MockMultipartFile("file", "profile.png", "image/png", new byte[]{}), HttpStatus.BAD_REQUEST),
				Arguments.of("profile photo rejects pdf",
						new MockMultipartFile("file", "profile.pdf", "application/pdf",
								new byte[]{0x25, 0x50, 0x44, 0x46}),
						HttpStatus.UNSUPPORTED_MEDIA_TYPE),
				Arguments.of("profile photo rejects svg",
						new MockMultipartFile("file", "profile.svg", "image/svg+xml", "<svg></svg>".getBytes()),
						HttpStatus.UNSUPPORTED_MEDIA_TYPE));
	}

	static Stream<Arguments> invalidOdontogramFiles() {
		return Stream.of(
				Arguments.of("empty odontogram",
						new MockMultipartFile("file", "odontogram.pdf", "application/pdf", new byte[]{}),
						HttpStatus.BAD_REQUEST),
				Arguments.of("odontogram rejects generic binary",
						new MockMultipartFile("file", "odontogram.bin", "application/octet-stream",
								new byte[]{1, 2, 3}),
						HttpStatus.UNSUPPORTED_MEDIA_TYPE),
				Arguments.of("odontogram rejects signature mismatch", new MockMultipartFile("file", "odontogram.png",
						"image/png", new byte[]{0x25, 0x50, 0x44, 0x46}), HttpStatus.BAD_REQUEST));
	}

	private PatientEntity patient(String cpf) {
		PatientEntity entity = new PatientEntity();
		entity.setFullName("Paciente Stored File");
		entity.setCpf(cpf);
		entity.setPhone("11999999999");
		entity.setBirthDate(LocalDate.of(1990, 1, 1));
		entity.setActive(true);
		return entity;
	}

	private void ensureAppUserTable() {
		jdbcTemplate.execute("create table if not exists app_user (id uuid primary key)");
		jdbcTemplate.execute("alter table app_user add column if not exists keycloak_subject varchar(100)");
		jdbcTemplate.execute("alter table app_user add column if not exists keycloak_username varchar(150)");
		jdbcTemplate.execute("alter table app_user add column if not exists full_name varchar(150)");
		jdbcTemplate.execute("alter table app_user add column if not exists email varchar(150)");
		jdbcTemplate.execute("alter table app_user add column if not exists email_verified boolean default true");
		jdbcTemplate.execute("alter table app_user add column if not exists active boolean default true");
	}

	private void insertUser(UUID id, String subject, String email) {
		jdbcTemplate.update("""
				merge into app_user (id, keycloak_subject, keycloak_username, full_name, email, email_verified, active)
				key(id) values (?, ?, ?, ?, ?, true, true)
				""", id, subject, subject, subject, email);
	}
}
