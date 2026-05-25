package com.clinica.mariana.restms.storedfile.dto;

import java.time.OffsetDateTime;

public record PresignedUrlDto(String url, OffsetDateTime expiresAt) {
}
