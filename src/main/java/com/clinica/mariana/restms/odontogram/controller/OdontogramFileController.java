package com.clinica.mariana.restms.odontogram.controller;

import com.clinica.mariana.restms.odontogram.dto.OdontogramFileDto;
import com.clinica.mariana.restms.odontogram.service.OdontogramFileService;
import com.clinica.mariana.restms.storedfile.dto.PresignedUrlDto;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/odontogram-files")
public class OdontogramFileController {

	private final OdontogramFileService odontogramFileService;

	public OdontogramFileController(OdontogramFileService odontogramFileService) {
		this.odontogramFileService = odontogramFileService;
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public OdontogramFileDto findById(@PathVariable UUID id) {
		return odontogramFileService.findById(id);
	}

	@GetMapping("/{id}/download-url")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public PresignedUrlDto downloadUrl(@PathVariable UUID id) {
		return odontogramFileService.presignedDownloadUrl(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public void delete(@PathVariable UUID id) {
		odontogramFileService.delete(id);
	}
}
