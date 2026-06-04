package com.clinica.mariana.restms.odontogram.controller;

import com.clinica.mariana.restms.odontogram.dto.OdontogramEntryCreateDto;
import com.clinica.mariana.restms.odontogram.dto.OdontogramEntryDto;
import com.clinica.mariana.restms.odontogram.dto.OdontogramEntryUpdateDto;
import com.clinica.mariana.restms.odontogram.service.OdontogramEntryService;
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
@RequestMapping("/odontogram-entries")
public class OdontogramEntryController {

	private final OdontogramEntryService service;

	public OdontogramEntryController(OdontogramEntryService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public OdontogramEntryDto create(@Valid @RequestBody OdontogramEntryCreateDto request) {
		return service.create(request);
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public OdontogramEntryDto findById(@PathVariable UUID id) {
		return service.findById(id);
	}

	@GetMapping("/by-patient/{patientId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public List<OdontogramEntryDto> findByPatient(@PathVariable UUID patientId) {
		return service.findByPatient(patientId);
	}

	@GetMapping("/by-medical-record/{medicalRecordId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public List<OdontogramEntryDto> findByMedicalRecord(@PathVariable UUID medicalRecordId) {
		return service.findByMedicalRecord(medicalRecordId);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public OdontogramEntryDto update(@PathVariable UUID id, @Valid @RequestBody OdontogramEntryUpdateDto request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public void delete(@PathVariable UUID id) {
		service.delete(id);
	}
}
