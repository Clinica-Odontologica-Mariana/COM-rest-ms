package com.clinica.mariana.restms.appointment.controller;

import com.clinica.mariana.restms.appointment.dto.AppointmentStatusDto;
import com.clinica.mariana.restms.appointment.repository.AppointmentStatusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/appointment-statuses")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Appointment Statuses", description = "Status disponíveis para consultas")
public class AppointmentStatusController {

	private final AppointmentStatusRepository repository;

	public AppointmentStatusController(AppointmentStatusRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	@Operation(summary = "Listar status de consulta", description = "Retorna todos os status disponíveis para consultas")
	public List<AppointmentStatusDto> findAll() {
		return repository.findAll().stream()
				.map(s -> new AppointmentStatusDto(s.getId(), s.getCode(), s.getName(),
						s.isBlocksSchedule(), s.isFinalStatus()))
				.toList();
	}
}
