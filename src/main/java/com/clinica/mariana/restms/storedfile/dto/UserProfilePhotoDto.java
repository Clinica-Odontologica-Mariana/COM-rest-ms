package com.clinica.mariana.restms.storedfile.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfilePhotoDto(UUID id, UUID userId, StoredFileDto file, OffsetDateTime createdAt) {
}
