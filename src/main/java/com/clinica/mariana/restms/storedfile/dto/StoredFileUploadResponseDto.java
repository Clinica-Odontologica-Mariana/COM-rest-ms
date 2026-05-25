package com.clinica.mariana.restms.storedfile.dto;

import java.util.UUID;

public record StoredFileUploadResponseDto(UUID id, StoredFileDto file) {
}
