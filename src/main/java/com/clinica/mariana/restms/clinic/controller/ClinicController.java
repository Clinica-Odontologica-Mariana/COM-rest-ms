package com.clinica.mariana.restms.clinic.controller;

import com.clinica.mariana.restms.clinic.dto.ClinicCreateDto;
import com.clinica.mariana.restms.clinic.dto.ClinicDto;
import com.clinica.mariana.restms.clinic.dto.ClinicUpdateDto;
import com.clinica.mariana.restms.clinic.service.ClinicService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clinics")
public class ClinicController {

	private final ClinicService clinicService;

	public ClinicController(ClinicService clinicService) {
		this.clinicService = clinicService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public ClinicDto create(@Valid @RequestBody ClinicCreateDto request) {
		return clinicService.create(request);
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public Page<ClinicDto> findAll(@PageableDefault(size = 20) Pageable pageable) {
		return clinicService.findAll(pageable);
	}

	@GetMapping("/document/{document}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public ClinicDto findByDocument(@PathVariable String document) {
		return clinicService.findByDocument(document);
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public ClinicDto findById(@PathVariable UUID id) {
		return clinicService.findById(id);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public ClinicDto update(@PathVariable UUID id, @Valid @RequestBody ClinicUpdateDto request) {
		return clinicService.update(id, request);
	}

	@PatchMapping("/{id}/inactivate")
	@RolesAllowed("ADMIN")
	public ClinicDto inactivate(@PathVariable UUID id) {
		return clinicService.inactivate(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed("ADMIN")
	public void delete(@PathVariable UUID id) {
		clinicService.delete(id);
	}
}
