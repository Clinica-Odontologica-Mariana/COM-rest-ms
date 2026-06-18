package com.clinica.mariana.restms.certificate.dto;

import jakarta.validation.constraints.NotNull;

public record CertificateFeaturedRequest(@NotNull Boolean featured) {
}
