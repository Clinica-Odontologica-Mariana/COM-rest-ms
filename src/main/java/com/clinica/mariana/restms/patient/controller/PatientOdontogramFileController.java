package com.clinica.mariana.restms.patient.controller;

import com.clinica.mariana.restms.odontogram.dto.OdontogramFileDto;
import com.clinica.mariana.restms.odontogram.service.OdontogramFileService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients/{patientId}/odontogram-files")
public class PatientOdontogramFileController {

	private final OdontogramFileService odontogramFileService;

	public PatientOdontogramFileController(OdontogramFileService odontogramFileService) {
		this.odontogramFileService = odontogramFileService;
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public List<OdontogramFileDto> findByPatient(@PathVariable UUID patientId) {
		return odontogramFileService.findByPatient(patientId);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public OdontogramFileDto upload(@PathVariable UUID patientId, @RequestParam(required = false) UUID medicalRecordId,
			@RequestParam(required = false) UUID odontogramEntryId, @RequestParam(required = false) String description,
			@RequestParam MultipartFile file) {
		return odontogramFileService.upload(patientId, medicalRecordId, odontogramEntryId, description, file, null);
	}
}
