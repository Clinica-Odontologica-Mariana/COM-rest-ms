package com.clinica.mariana.restms.professional.controller;

import com.clinica.mariana.restms.professional.dto.ProfessionalCreateDto;
import com.clinica.mariana.restms.professional.dto.ProfessionalDto;
import com.clinica.mariana.restms.professional.dto.ProfessionalUpdateDto;
import com.clinica.mariana.restms.professional.service.ProfessionalService;
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
@RequestMapping("/professionals")
public class ProfessionalController {

	private final ProfessionalService professionalService;

	public ProfessionalController(ProfessionalService professionalService) {
		this.professionalService = professionalService;
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
}
