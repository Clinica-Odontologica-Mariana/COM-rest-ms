package com.clinica.mariana.restms.storedfile.controller;

import com.clinica.mariana.restms.storedfile.dto.OdontogramFileDto;
import com.clinica.mariana.restms.storedfile.dto.PresignedUrlDto;
import com.clinica.mariana.restms.storedfile.service.OdontogramFileService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/stored-files")
public class StoredFileController {

	private final OdontogramFileService odontogramFileService;

	public StoredFileController(OdontogramFileService odontogramFileService) {
		this.odontogramFileService = odontogramFileService;
	}

	@PostMapping(value = "/odontograms/{patientId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public OdontogramFileDto uploadOdontogram(@PathVariable UUID patientId,
			@RequestParam(required = false) UUID medicalRecordId,
			@RequestParam(required = false) UUID odontogramEntryId, @RequestParam(required = false) String description,
			@RequestParam MultipartFile file) {
		return odontogramFileService.upload(patientId, medicalRecordId, odontogramEntryId, description, file, null);
	}

	@GetMapping("/odontograms/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public OdontogramFileDto findOdontogram(@PathVariable UUID id) {
		return odontogramFileService.findById(id);
	}

	@GetMapping("/odontograms/{id}/download-url")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public PresignedUrlDto odontogramDownloadUrl(@PathVariable UUID id) {
		return odontogramFileService.presignedDownloadUrl(id);
	}

	@DeleteMapping("/odontograms/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public void deleteOdontogram(@PathVariable UUID id) {
		odontogramFileService.delete(id);
	}
}
