package com.clinica.mariana.restms.patient.controller;

import com.clinica.mariana.restms.patient.dto.PatientCreateDto;
import com.clinica.mariana.restms.patient.dto.PatientDto;
import com.clinica.mariana.restms.patient.dto.PatientUpdateDto;
import com.clinica.mariana.restms.patient.service.PatientService;
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
@RequestMapping("/api/v1/patients")
public class PatientController {

	private final PatientService patientService;

	public PatientController(PatientService patientService) {
		this.patientService = patientService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PatientDto create(@Valid @RequestBody PatientCreateDto request) {
		return patientService.create(request);
	}

	@GetMapping
	public List<PatientDto> findAll() {
		return patientService.findAll();
	}

	@GetMapping("/{id}")
	public PatientDto findById(@PathVariable UUID id) {
		return patientService.findById(id);
	}

	@GetMapping("/{cpf}")
	public PatientDto findByCPF(@PathVariable String cpf) {
		return patientService.findByCpf(cpf);
	}

	@PutMapping("/{id}")
	public PatientDto update(@PathVariable UUID id, @Valid @RequestBody PatientUpdateDto request) {
		return patientService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		patientService.delete(id);
	}

	@GetMapping("/example")
	public PatientDto example() {
		return patientService.example();
	}
}
