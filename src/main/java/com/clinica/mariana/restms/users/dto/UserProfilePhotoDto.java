package com.clinica.mariana.restms.users.dto;

import com.clinica.mariana.restms.storedfile.dto.StoredFileDto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfilePhotoDto(UUID id, UUID userId, StoredFileDto file, OffsetDateTime createdAt) {
}
