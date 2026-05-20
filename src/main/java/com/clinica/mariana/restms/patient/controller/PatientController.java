package com.clinica.mariana.restms.patient.controller;

import com.clinica.mariana.restms.patient.dto.PatientCreateDto;
import com.clinica.mariana.restms.patient.dto.PatientDto;
import com.clinica.mariana.restms.patient.dto.PatientUpdateDto;
import com.clinica.mariana.restms.patient.service.PatientService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
public class PatientController {

	private final PatientService patientService;

	public PatientController(PatientService patientService) {
		this.patientService = patientService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public PatientDto create(@Valid @RequestBody PatientCreateDto request) {
		return patientService.create(request);
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public List<PatientDto> findAll() {
		return patientService.findAll();
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public PatientDto findById(@PathVariable UUID id) {
		return patientService.findById(id);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public PatientDto update(@PathVariable UUID id, @Valid @RequestBody PatientUpdateDto request) {
		return patientService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed("ADMIN")
	public void delete(@PathVariable UUID id) {
		patientService.delete(id);
	}
}
