package com.clinica.mariana.restms.certificate.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CertificatePublicDto(UUID id, String title, String certificateType, OffsetDateTime issuedAt) {
}
