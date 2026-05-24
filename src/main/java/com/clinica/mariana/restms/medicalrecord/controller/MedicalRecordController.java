package com.clinica.mariana.restms.medicalrecord.controller;

import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteDto;
import com.clinica.mariana.restms.medicalrecord.service.MedicalRecordService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/medical-records")
public class MedicalRecordController {

	private final MedicalRecordService medicalRecordService;

	public MedicalRecordController(MedicalRecordService medicalRecordService) {
		this.medicalRecordService = medicalRecordService;
	}

	@GetMapping("/by-patient/{patientId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordDto findByPatient(@PathVariable UUID patientId) {
		return medicalRecordService.findByPatientId(patientId);
	}

	@PostMapping("/by-patient/{patientId}/notes")
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordNoteDto addNote(@PathVariable UUID patientId,
			@Valid @RequestBody MedicalRecordNoteCreateDto request) {
		return medicalRecordService.addNote(patientId, request);
	}

	@PostMapping("/by-patient/{patientId}/attachments")
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordAttachmentDto addAttachment(@PathVariable UUID patientId,
			@Valid @RequestBody MedicalRecordAttachmentCreateDto request) {
		return medicalRecordService.addAttachment(patientId, request);
	}
}
