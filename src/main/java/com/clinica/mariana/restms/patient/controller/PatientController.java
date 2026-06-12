package com.clinica.mariana.restms.patient.controller;

import com.clinica.mariana.restms.patient.dto.PatientCreateDto;
import com.clinica.mariana.restms.patient.dto.PatientClinicCreateDto;
import com.clinica.mariana.restms.patient.dto.PatientClinicDto;
import com.clinica.mariana.restms.patient.dto.PatientDto;
import com.clinica.mariana.restms.patient.dto.PatientUpdateDto;
import com.clinica.mariana.restms.patient.service.PatientClinicService;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
public class PatientController {

	private final PatientService patientService;
	private final PatientClinicService patientClinicService;

	public PatientController(PatientService patientService, PatientClinicService patientClinicService) {
		this.patientService = patientService;
		this.patientClinicService = patientClinicService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public PatientDto create(@Valid @RequestBody PatientCreateDto request) {
		return patientService.create(request);
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public Page<PatientDto> findAll(@PageableDefault(size = 20) Pageable pageable) {
		return patientService.findAll(pageable);
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public PatientDto findById(@PathVariable UUID id) {
		return patientService.findById(id);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public PatientDto update(@PathVariable UUID id, @Valid @RequestBody PatientUpdateDto request) {
		return patientService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public void delete(@PathVariable UUID id) {
		patientService.delete(id);
	}

	@PostMapping("/{patientId}/clinics")
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public PatientClinicDto addClinic(@PathVariable UUID patientId,
			@Valid @RequestBody PatientClinicCreateDto request) {
		return patientClinicService.create(patientId, request);
	}

	@GetMapping("/{patientId}/clinics")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public List<PatientClinicDto> findClinics(@PathVariable UUID patientId) {
		return patientClinicService.findByPatient(patientId);
	}

	@PutMapping("/{patientId}/clinics/{clinicId}/primary")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public PatientClinicDto setPrimaryClinic(@PathVariable UUID patientId, @PathVariable UUID clinicId) {
		return patientClinicService.setPrimary(patientId, clinicId);
	}

	@DeleteMapping("/{patientId}/clinics/{clinicId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public void removeClinic(@PathVariable UUID patientId, @PathVariable UUID clinicId) {
		patientClinicService.deactivate(patientId, clinicId);
	}
}
