package com.clinica.mariana.restms.storedfile.controller;

import com.clinica.mariana.restms.storedfile.dto.OdontogramFileDto;
import com.clinica.mariana.restms.storedfile.service.OdontogramFileService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
