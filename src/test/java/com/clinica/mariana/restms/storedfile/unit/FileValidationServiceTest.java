package com.clinica.mariana.restms.storedfile.unit;

import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.storedfile.config.FileValidationProperties;
import com.clinica.mariana.restms.storedfile.model.FileCategory;
import com.clinica.mariana.restms.storedfile.service.FileValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockMultipartFile;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("File validation")
class FileValidationServiceTest {

	private FileValidationService service;

	@BeforeEach
	void setUp() {
		FileValidationProperties properties = new FileValidationProperties(
				new FileValidationProperties.FilePolicy(10, "image/jpeg,image/png,image/webp"),
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

	static Stream<Arguments> validFiles() {
		return Stream.of(
				Arguments.of("profile jpeg", FileCategory.USER_PROFILE_PHOTO, "image/jpeg",
						new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01}),
				Arguments.of("profile png", FileCategory.USER_PROFILE_PHOTO, "image/png",
						new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
				Arguments.of("odontogram pdf", FileCategory.ODONTOGRAM, "application/pdf",
						new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D}));
	}

	static Stream<Arguments> invalidFiles() {
		return Stream.of(Arguments.of("empty file", FileCategory.USER_PROFILE_PHOTO, "image/jpeg", new byte[]{}),
				Arguments.of("unsupported profile pdf", FileCategory.USER_PROFILE_PHOTO, "application/pdf",
						new byte[]{0x25, 0x50, 0x44, 0x46}),
				Arguments.of("signature mismatch", FileCategory.ODONTOGRAM, "image/png",
						new byte[]{0x25, 0x50, 0x44, 0x46}),
				Arguments.of("too large", FileCategory.USER_PROFILE_PHOTO, "image/jpeg",
						new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3, 4, 5, 6, 7, 8}));
	}
}
