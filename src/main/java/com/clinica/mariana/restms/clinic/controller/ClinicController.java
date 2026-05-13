package com.clinica.mariana.restms.clinic.controller;

import com.clinica.mariana.restms.clinic.dto.ClinicCreateDto;
import com.clinica.mariana.restms.clinic.dto.ClinicDto;
import com.clinica.mariana.restms.clinic.dto.ClinicUpdateDto;
import com.clinica.mariana.restms.clinic.service.ClinicService;
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
@RequestMapping("/api/v1/clinics")
public class ClinicController {

	private final ClinicService clinicService;

	public ClinicController(ClinicService clinicService) {
		this.clinicService = clinicService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ClinicDto create(@Valid @RequestBody ClinicCreateDto request) {
		return clinicService.create(request);
	}

	@GetMapping
	public List<ClinicDto> findAll() {
		return clinicService.findAll();
	}

	@GetMapping("/{id}")
	public ClinicDto findById(@PathVariable UUID id) {
		return clinicService.findById(id);
	}

	@PutMapping("/{id}")
	public ClinicDto update(@PathVariable UUID id, @Valid @RequestBody ClinicUpdateDto request) {
		return clinicService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		clinicService.delete(id);
	}
}
