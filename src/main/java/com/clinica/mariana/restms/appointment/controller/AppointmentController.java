package com.clinica.mariana.restms.appointment.controller;

import com.clinica.mariana.restms.appointment.dto.AppointmentCreateDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentUpdateDto;
import com.clinica.mariana.restms.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@PreAuthorize("isAuthenticated()")
public class AppointmentController {

	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AppointmentDto create(@Valid @RequestBody AppointmentCreateDto request) {
		return appointmentService.create(request);
	}

	@GetMapping
	public List<AppointmentDto> findAll() {
		return appointmentService.findAll();
	}

	@GetMapping("/period")
	public List<AppointmentDto> findByPeriod(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {
		return appointmentService.findByPeriod(start, end);
	}

	@PutMapping("/{id}")
	public AppointmentDto update(@PathVariable UUID id, @Valid @RequestBody AppointmentUpdateDto request) {
		return appointmentService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		appointmentService.delete(id);
	}
}
