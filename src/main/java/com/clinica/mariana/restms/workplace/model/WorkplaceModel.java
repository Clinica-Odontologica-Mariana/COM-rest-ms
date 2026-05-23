package com.clinica.mariana.restms.workplace.model;

import java.util.UUID;

public record WorkplaceModel(
        UUID id,
        UUID clinicId,
        String name,
        String description,
        boolean active
) {
    public WorkplaceModel {
        if (clinicId == null) {
            throw new IllegalArgumentException("clinicId is required");
        }

        requireNotBlank(name, "name");
    }

    public static WorkplaceModel create(
            UUID clinicId,
            String name,
            String description
    ) {
        return new WorkplaceModel(null, clinicId, name, description, true);
    }

    public WorkplaceModel withId(UUID id) {
        return new WorkplaceModel(id, clinicId, name, description, active);
    }

    private static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}
