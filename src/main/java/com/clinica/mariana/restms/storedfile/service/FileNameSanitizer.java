package com.clinica.mariana.restms.storedfile.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.Normalizer;

@Component
public class FileNameSanitizer {

	private static final String DEFAULT_FILE_NAME = "file";
	private static final int MAX_FILE_NAME_LENGTH = 120;

	public String sanitize(String originalFileName) {
		String normalizedPath = originalFileName == null ? null : originalFileName.replace('\\', '/');
		String candidate = StringUtils.getFilename(normalizedPath);
		if (!StringUtils.hasText(candidate)) {
			return DEFAULT_FILE_NAME;
		}
		String sanitized = Normalizer.normalize(candidate, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		sanitized = sanitized.replaceAll("\\p{Cntrl}", "");
		sanitized = sanitized.replaceAll("\\s+", "-").replaceAll("[^A-Za-z0-9._-]", "-").replaceAll("-+", "-");
		sanitized = sanitized.replaceAll("^[.-]+", "").replaceAll("[.-]+$", "");
		if (!StringUtils.hasText(sanitized)) {
			return DEFAULT_FILE_NAME;
		}
		return limitLength(sanitized);
	}

	private String limitLength(String sanitized) {
		if (sanitized.length() <= MAX_FILE_NAME_LENGTH) {
			return sanitized;
		}
		int extensionStart = sanitized.lastIndexOf('.');
		if (extensionStart > 0 && sanitized.length() - extensionStart <= 16) {
			String extension = sanitized.substring(extensionStart);
			String baseName = sanitized.substring(0, extensionStart);
			return baseName.substring(0, MAX_FILE_NAME_LENGTH - extension.length()) + extension;
		}
		return sanitized.substring(0, MAX_FILE_NAME_LENGTH);
	}
}
