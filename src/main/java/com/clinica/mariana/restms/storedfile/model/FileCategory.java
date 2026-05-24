package com.clinica.mariana.restms.storedfile.model;

public enum FileCategory {
	ODONTOGRAM("odontograms"), USER_PROFILE_PHOTO("profile-photos");

	private final String objectKeyPrefix;

	FileCategory(String objectKeyPrefix) {
		this.objectKeyPrefix = objectKeyPrefix;
	}

	public String objectKeyPrefix() {
		return objectKeyPrefix;
	}
}
