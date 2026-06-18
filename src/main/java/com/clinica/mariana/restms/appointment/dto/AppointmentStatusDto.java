package com.clinica.mariana.restms.appointment.dto;

import java.util.UUID;

public record AppointmentStatusDto(UUID id, String code, String name, boolean blocksSchedule,
		boolean finalStatus) {
}
