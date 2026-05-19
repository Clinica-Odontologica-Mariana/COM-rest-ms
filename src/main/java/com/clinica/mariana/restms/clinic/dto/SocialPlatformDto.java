package com.clinica.mariana.restms.clinic.dto;

import java.util.UUID;

public record SocialPlataformDto(
        UUID id,
        String code,
        String name
) {
}
