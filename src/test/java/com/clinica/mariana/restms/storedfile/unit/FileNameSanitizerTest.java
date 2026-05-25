package com.clinica.mariana.restms.storedfile.unit;

import com.clinica.mariana.restms.storedfile.service.FileNameSanitizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("File name sanitizer")
class FileNameSanitizerTest {

	private final FileNameSanitizer sanitizer = new FileNameSanitizer();

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({"photo.png,photo.png", "../../secret.txt,secret.txt", "..\\..\\secret.txt,secret.txt",
			"my profile photo.png,my-profile-photo.png", "foto ä ç.jpg,foto-a-c.jpg", ".env,env",
			"script.svg,script.svg", "file with spaces.jpeg,file-with-spaces.jpeg", "***,file"})
	void shouldSanitizeDangerousFileNames(String originalFileName, String expected) {
		assertThat(sanitizer.sanitize(originalFileName)).isEqualTo(expected);
	}

	@ParameterizedTest
	@NullAndEmptySource
	void shouldFallbackWhenFileNameIsBlank(String originalFileName) {
		assertThat(sanitizer.sanitize(originalFileName)).isEqualTo("file");
	}

	@Test
	void shouldLimitLongFileNameAndPreserveExtension() {
		String sanitized = sanitizer.sanitize("a".repeat(200) + ".png");

		assertThat(sanitized).hasSize(120).endsWith(".png");
	}
}
