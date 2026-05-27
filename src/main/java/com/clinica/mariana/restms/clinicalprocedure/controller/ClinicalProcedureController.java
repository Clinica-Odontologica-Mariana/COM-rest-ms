package com.clinica.mariana.restms.clinicalprocedure.controller;

import com.clinica.mariana.restms.clinicalprocedure.dto.ClinicalProcedureCreateDto;
import com.clinica.mariana.restms.clinicalprocedure.dto.ClinicalProcedureDto;
import com.clinica.mariana.restms.clinicalprocedure.dto.ClinicalProcedureUpdateDto;
import com.clinica.mariana.restms.clinicalprocedure.service.ClinicalProcedureService;
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
@RequestMapping("/clinical-procedures")
public class ClinicalProcedureController {

	private final ClinicalProcedureService service;

	public ClinicalProcedureController(ClinicalProcedureService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public ClinicalProcedureDto create(@Valid @RequestBody ClinicalProcedureCreateDto request) {
		return service.create(request);
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public List<ClinicalProcedureDto> findAll() {
		return service.findAll();
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public ClinicalProcedureDto findById(@PathVariable UUID id) {
		return service.findById(id);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public ClinicalProcedureDto update(@PathVariable UUID id, @Valid @RequestBody ClinicalProcedureUpdateDto request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed("ADMIN")
	public void delete(@PathVariable UUID id) {
		service.delete(id);
	}
}
