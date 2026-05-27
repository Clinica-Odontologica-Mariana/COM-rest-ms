package com.clinica.mariana.restms.professional.controller;

import java.util.List;
import java.util.UUID;

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

import com.clinica.mariana.restms.professional.dto.ProfessionalClinicCreateDto;
import com.clinica.mariana.restms.professional.dto.ProfessionalClinicDto;
import com.clinica.mariana.restms.professional.dto.ProfessionalCreateDto;
import com.clinica.mariana.restms.professional.dto.ProfessionalDto;
import com.clinica.mariana.restms.professional.dto.ProfessionalUpdateDto;
import com.clinica.mariana.restms.professional.service.ProfessionalClinicService;
import com.clinica.mariana.restms.professional.service.ProfessionalService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/professionals")
public class ProfessionalController {

	private final ProfessionalService professionalService;
	private final ProfessionalClinicService professionalClinicService;

	public ProfessionalController(ProfessionalService professionalService,
			ProfessionalClinicService professionalClinicService) {
		this.professionalService = professionalService;
		this.professionalClinicService = professionalClinicService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public ProfessionalDto create(@Valid @RequestBody ProfessionalCreateDto request) {
		return professionalService.create(request);
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public List<ProfessionalDto> findAll() {
		return professionalService.findAll();
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public ProfessionalDto findById(@PathVariable UUID id) {
		return professionalService.findById(id);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public ProfessionalDto update(@PathVariable UUID id, @Valid @RequestBody ProfessionalUpdateDto request) {
		return professionalService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed("ADMIN")
	public void delete(@PathVariable UUID id) {
		professionalService.delete(id);
	}

	@PostMapping("/{professionalId}/clinics")
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public ProfessionalClinicDto addClinic(@PathVariable UUID professionalId,
			@Valid @RequestBody ProfessionalClinicCreateDto request) {
		return professionalClinicService.create(professionalId, request);
	}

	@GetMapping("/{professionalId}/clinics")
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public List<ProfessionalClinicDto> findClinics(@PathVariable UUID professionalId) {
		return professionalClinicService.findByProfessional(professionalId);
	}

	@PutMapping("/{professionalId}/clinics/{clinicId}/primary")
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public ProfessionalClinicDto setPrimaryClinic(@PathVariable UUID professionalId, @PathVariable UUID clinicId) {
		return professionalClinicService.setPrimary(professionalId, clinicId);
	}

	@DeleteMapping("/{professionalId}/clinics/{clinicId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public void removeClinic(@PathVariable UUID professionalId, @PathVariable UUID clinicId) {
		professionalClinicService.deactivate(professionalId, clinicId);
	}
}
