package com.clinica.mariana.restms.clinic.dto;

import java.util.UUID;

public record SocialLinkDto(UUID id, UUID clinicId, UUID platformId, String url) {
}
