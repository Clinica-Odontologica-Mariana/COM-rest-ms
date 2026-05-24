package com.clinica.mariana.restms.storedfile.unit;

import com.clinica.mariana.restms.storedfile.service.FileNameSanitizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("File name sanitizer")
class FileNameSanitizerTest {

	private final FileNameSanitizer sanitizer = new FileNameSanitizer();

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({"photo.png,photo.png", "../secret.png,secret.png", "..\\secret.png,secret.png",
			"my profile photo.png,my-profile-photo.png", "***,file"})
	void shouldSanitizeDangerousFileNames(String originalFileName, String expected) {
		assertThat(sanitizer.sanitize(originalFileName)).isEqualTo(expected);
	}
}
