package com.clinica.mariana.restms.storedfile.unit;

import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.storedfile.config.FileValidationProperties;
import com.clinica.mariana.restms.storedfile.model.FileCategory;
import com.clinica.mariana.restms.storedfile.service.FileValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("File validation")
class FileValidationServiceTest {

	private FileValidationService service;

	@BeforeEach
	void setUp() {
		FileValidationProperties properties = new FileValidationProperties(
				new FileValidationProperties.FilePolicy(12, "image/jpeg,image/png,image/webp"),
				new FileValidationProperties.FilePolicy(20, "image/jpeg,image/png,image/webp,application/pdf"));
		service = new FileValidationService(properties);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("validFiles")
	void shouldAcceptAllowedFiles(String scenario, FileCategory category, String mimeType, byte[] content) {
		MockMultipartFile file = new MockMultipartFile("file", "valid.bin", mimeType, content);

		assertThatCode(() -> service.validate(category, file)).doesNotThrowAnyException();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidFiles")
	void shouldRejectInvalidFiles(String scenario, FileCategory category, String mimeType, byte[] content) {
		MockMultipartFile file = new MockMultipartFile("file", "invalid.bin", mimeType, content);

		assertThatThrownBy(() -> service.validate(category, file)).isInstanceOf(AppException.class);
	}

	@Test
	void shouldRejectMissingCategoryWithControlledError() {
		MockMultipartFile file = new MockMultipartFile("file", "profile.png", "image/png",
				new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});

		assertThatThrownBy(() -> service.validate(null, file)).isInstanceOf(AppException.class).extracting("status")
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	static Stream<Arguments> validFiles() {
		return Stream.of(
				Arguments.of("profile jpeg", FileCategory.USER_PROFILE_PHOTO, "image/jpeg",
						new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01}),
				Arguments.of("profile png", FileCategory.USER_PROFILE_PHOTO, "image/png",
						new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
				Arguments.of("profile webp", FileCategory.USER_PROFILE_PHOTO, "image/webp",
						new byte[]{0x52, 0x49, 0x46, 0x46, 0x01, 0x02, 0x03, 0x04, 0x57, 0x45, 0x42, 0x50}),
				Arguments.of("odontogram png", FileCategory.ODONTOGRAM, "image/png",
						new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
				Arguments.of("odontogram pdf", FileCategory.ODONTOGRAM, "application/pdf",
						new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D}));
	}

	static Stream<Arguments> invalidFiles() {
		return Stream.of(Arguments.of("empty file", FileCategory.USER_PROFILE_PHOTO, "image/jpeg", new byte[]{}),
				Arguments.of("unsupported profile pdf", FileCategory.USER_PROFILE_PHOTO, "application/pdf",
						new byte[]{0x25, 0x50, 0x44, 0x46}),
				Arguments.of("unsupported profile svg", FileCategory.USER_PROFILE_PHOTO, "image/svg+xml",
						"<svg></svg>".getBytes()),
				Arguments.of("signature mismatch", FileCategory.ODONTOGRAM, "image/png",
						new byte[]{0x25, 0x50, 0x44, 0x46}),
				Arguments.of("too large", FileCategory.USER_PROFILE_PHOTO, "image/jpeg",
						new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}),
				Arguments.of("non-uploadable medical attachment category", FileCategory.MEDICAL_RECORD_ATTACHMENT,
						"image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
				Arguments.of("non-uploadable certificate category", FileCategory.CERTIFICATE, "image/png",
						new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}));
	}
}
