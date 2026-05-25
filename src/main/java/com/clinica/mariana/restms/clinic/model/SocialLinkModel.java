package com.clinica.mariana.restms.clinic.model;

import java.util.UUID;

public record SocialLinkModel(UUID id, UUID clinicId, UUID platformId, String url) {
	public SocialLinkModel {
		if (clinicId == null) {
			throw new IllegalArgumentException("clinicId is required");
		}

		if (platformId == null) {
			throw new IllegalArgumentException("platformId is required");
		}

		url = requireNotBlank(url, "url");

		if (!url.matches("(?i)^https?://.*")) {
			throw new IllegalArgumentException("url must start with http:// or https://");
		}
	}

	public static SocialLinkModel create(UUID clinicId, UUID platformId, String url) {
		return new SocialLinkModel(null, clinicId, platformId, url);
	}

	public SocialLinkModel withId(UUID id) {
		return new SocialLinkModel(id, clinicId, platformId, url);
	}

	private static String requireNotBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}

		return value;
	}
}
