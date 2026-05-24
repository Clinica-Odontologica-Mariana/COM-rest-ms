package com.clinica.mariana.restms.medicalrecord.controller;

import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentUpdateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteUpdateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordUpdateDto;
import com.clinica.mariana.restms.medicalrecord.service.MedicalRecordService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/medical-records")
public class MedicalRecordController {

	private final MedicalRecordService medicalRecordService;

	public MedicalRecordController(MedicalRecordService medicalRecordService) {
		this.medicalRecordService = medicalRecordService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordDto create(@Valid @RequestBody MedicalRecordCreateDto request) {
		return medicalRecordService.create(request);
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public List<MedicalRecordDto> findAll() {
		return medicalRecordService.findAll();
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordDto findById(@PathVariable UUID id) {
		return medicalRecordService.findById(id);
	}

	@GetMapping("/by-patient/{patientId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordDto findByPatientId(@PathVariable UUID patientId) {
		return medicalRecordService.findByPatientId(patientId);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordDto update(@PathVariable UUID id, @Valid @RequestBody MedicalRecordUpdateDto request) {
		return medicalRecordService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed("ADMIN")
	public void delete(@PathVariable UUID id) {
		medicalRecordService.delete(id);
	}

	@PostMapping("/by-patient/{patientId}/notes")
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordNoteDto addNote(@PathVariable UUID patientId,
			@Valid @RequestBody MedicalRecordNoteCreateDto request, @AuthenticationPrincipal Jwt jwt) {
		return medicalRecordService.addNote(patientId, currentUserId(jwt).orElse(null), request);
	}

	@GetMapping("/by-patient/{patientId}/notes")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public List<MedicalRecordNoteDto> findNotesByPatientId(@PathVariable UUID patientId) {
		return medicalRecordService.findNotesByPatientId(patientId);
	}

	@GetMapping("/by-patient/{patientId}/notes/{noteId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordNoteDto findNoteById(@PathVariable UUID patientId, @PathVariable UUID noteId) {
		return medicalRecordService.findNoteById(patientId, noteId);
	}

	@PutMapping("/by-patient/{patientId}/notes/{noteId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordNoteDto updateNote(@PathVariable UUID patientId, @PathVariable UUID noteId,
			@Valid @RequestBody MedicalRecordNoteUpdateDto request) {
		return medicalRecordService.updateNote(patientId, noteId, request);
	}

	@DeleteMapping("/by-patient/{patientId}/notes/{noteId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public void deleteNote(@PathVariable UUID patientId, @PathVariable UUID noteId) {
		medicalRecordService.deleteNote(patientId, noteId);
	}

	@PostMapping("/by-patient/{patientId}/attachments")
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordAttachmentDto addAttachment(@PathVariable UUID patientId,
			@Valid @RequestBody MedicalRecordAttachmentCreateDto request) {
		return medicalRecordService.addAttachment(patientId, request);
	}

	@GetMapping("/by-patient/{patientId}/attachments")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public List<MedicalRecordAttachmentDto> findAttachmentsByPatientId(@PathVariable UUID patientId) {
		return medicalRecordService.findAttachmentsByPatientId(patientId);
	}

	@GetMapping("/by-patient/{patientId}/attachments/{attachmentId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordAttachmentDto findAttachmentById(@PathVariable UUID patientId,
			@PathVariable UUID attachmentId) {
		return medicalRecordService.findAttachmentById(patientId, attachmentId);
	}

	@PutMapping("/by-patient/{patientId}/attachments/{attachmentId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public MedicalRecordAttachmentDto updateAttachment(@PathVariable UUID patientId, @PathVariable UUID attachmentId,
			@Valid @RequestBody MedicalRecordAttachmentUpdateDto request) {
		return medicalRecordService.updateAttachment(patientId, attachmentId, request);
	}

	@DeleteMapping("/by-patient/{patientId}/attachments/{attachmentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public void deleteAttachment(@PathVariable UUID patientId, @PathVariable UUID attachmentId) {
		medicalRecordService.deleteAttachment(patientId, attachmentId);
	}

	private Optional<UUID> currentUserId(Jwt jwt) {
		if (jwt == null) {
			return Optional.empty();
		}

		String appUserId = jwt.getClaimAsString("app_user_id");
		if (appUserId != null && !appUserId.isBlank()) {
			return parseUuid(appUserId);
		}

		return parseUuid(jwt.getSubject());
	}

	private Optional<UUID> parseUuid(String value) {
		try {
			return value == null || value.isBlank() ? Optional.empty() : Optional.of(UUID.fromString(value));
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}
}
