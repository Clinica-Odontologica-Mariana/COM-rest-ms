package com.clinica.mariana.restms.medicalrecord.controller;

import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordUpdateDto;
import com.clinica.mariana.restms.medicalrecord.service.MedicalRecordService;
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
@RequestMapping("/api/v1/medical-records")
public class MedicalRecordController {

	private final MedicalRecordService medicalRecordService;

	public MedicalRecordController(MedicalRecordService medicalRecordService) {
		this.medicalRecordService = medicalRecordService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MedicalRecordDto create(@Valid @RequestBody MedicalRecordCreateDto request) {
		return medicalRecordService.create(request);
	}

	@GetMapping
	public List<MedicalRecordDto> findAll() {
		return medicalRecordService.findAll();
	}

	@GetMapping("/{id}")
	public MedicalRecordDto findById(@PathVariable UUID id) {
		return medicalRecordService.findById(id);
	}

	@GetMapping("/patient/{patientId}")
	public MedicalRecordDto findByPatientId(@PathVariable UUID patientId) {
		return medicalRecordService.findByPatientId(patientId);
	}

	@PutMapping("/{id}")
	public MedicalRecordDto update(@PathVariable UUID id, @Valid @RequestBody MedicalRecordUpdateDto request) {
		return medicalRecordService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		medicalRecordService.delete(id);
	}
}
