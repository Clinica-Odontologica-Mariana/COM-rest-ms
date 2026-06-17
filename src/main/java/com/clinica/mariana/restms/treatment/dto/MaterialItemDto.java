package com.clinica.mariana.restms.treatment.dto;

import java.util.UUID;

public record MaterialItemDto(UUID id, String name, String category, Integer quantity) {
}
