package com.clinica.mariana.restms.appointment.controller;

import com.clinica.mariana.restms.appointment.dto.AppointmentCreateDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentUpdateDto;
import com.clinica.mariana.restms.appointment.service.AppointmentService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@PreAuthorize("isAuthenticated()")
public class AppointmentController {

	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public AppointmentDto create(@Valid @RequestBody AppointmentCreateDto request) {
		return appointmentService.create(request);
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public Page<AppointmentDto> findAll(@PageableDefault(size = 20) Pageable pageable) {
		return appointmentService.findAll(pageable);
	}

	@GetMapping("/period")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public Page<AppointmentDto> findByPeriod(@PageableDefault(size = 20) Pageable pageable,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
		return appointmentService.findByPeriod(start.atOffset(ZoneOffset.UTC), end.atOffset(ZoneOffset.UTC), pageable);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public AppointmentDto update(@PathVariable UUID id, @Valid @RequestBody AppointmentUpdateDto request) {
		return appointmentService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public void delete(@PathVariable UUID id) {
		appointmentService.delete(id);
	}
}
