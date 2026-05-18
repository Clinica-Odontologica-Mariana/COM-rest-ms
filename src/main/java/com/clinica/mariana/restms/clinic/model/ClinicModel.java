package com.clinica.mariana.restms.clinic.model;

import java.util.UUID;

public record ClinicModel(
        UUID id,
        UUID addressId,
        String name,
        String document,
        String phone,
        String email,
        String timezone,
        boolean active
) {
    public ClinicModel {
        name = requireNotBlank(name, "name");
        document = requireNotBlank(document, "document");
        phone = requireNotBlank(phone, "phone");
        timezone = (timezone == null || timezone.isBlank()) ? "America/Sao_Paulo" : timezone;

        if (!document.matches("^[0-9]{14}$")) {
            throw new IllegalArgumentException("document must contain exactly 14 digits (CNPJ)");
        }

        if (email != null && !email.isBlank() && !email.matches("(?i)^[A-Z0-9._%+\\-]+@[A-Z0-9.\\-]+\\.[A-Z]{2,}$")) {
            throw new IllegalArgumentException("email format is invalid");
        }
    }

    public static ClinicModel create(
            UUID addressId,
            String name,
            String document,
            String phone,
            String email,
            String timezone
    ) {
        return new ClinicModel(null, addressId, name, document, phone, email, timezone, true);
    }

    public ClinicModel withId(UUID id) {
        return new ClinicModel(id, addressId, name, document, phone, email, timezone, active);
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}