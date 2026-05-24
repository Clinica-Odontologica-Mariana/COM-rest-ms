package com.clinica.mariana.restms.storedfile.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FileNameSanitizer {

	private static final String DEFAULT_FILE_NAME = "file";

	public String sanitize(String originalFileName) {
		String candidate = StringUtils.getFilename(originalFileName);
		if (!StringUtils.hasText(candidate)) {
			return DEFAULT_FILE_NAME;
		}
		String sanitized = candidate.replace('\\', '/');
		sanitized = StringUtils.getFilename(sanitized);
		sanitized = sanitized.replaceAll("[^A-Za-z0-9._-]", "-").replaceAll("-+", "-");
		sanitized = sanitized.replaceAll("^[.-]+", "").replaceAll("[.-]+$", "");
		if (!StringUtils.hasText(sanitized)) {
			return DEFAULT_FILE_NAME;
		}
		return sanitized.length() > 120 ? sanitized.substring(sanitized.length() - 120) : sanitized;
	}
}
