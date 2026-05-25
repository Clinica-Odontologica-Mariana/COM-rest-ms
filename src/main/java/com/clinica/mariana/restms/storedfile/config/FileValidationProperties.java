package com.clinica.mariana.restms.storedfile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "app.files")
public record FileValidationProperties(FilePolicy profilePhoto, FilePolicy odontogram) {

	public record FilePolicy(long maxSizeBytes, String allowedMimeTypes) {

		public Set<String> allowedMimeTypeSet() {
			if (!StringUtils.hasText(allowedMimeTypes)) {
				return Set.of();
			}
			return Arrays.stream(allowedMimeTypes.split(",")).map(String::trim).filter(StringUtils::hasText)
					.collect(Collectors.toUnmodifiableSet());
		}
	}
}
