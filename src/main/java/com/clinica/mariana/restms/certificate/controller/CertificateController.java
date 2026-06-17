package com.clinica.mariana.restms.certificate.controller;

import com.clinica.mariana.restms.certificate.dto.CertificateCreateDto;
import com.clinica.mariana.restms.certificate.dto.CertificateDto;
import com.clinica.mariana.restms.certificate.dto.CertificateUpdateDto;
import com.clinica.mariana.restms.certificate.service.CertificateService;
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
@RequestMapping("/certificates")
public class CertificateController {

	private final CertificateService service;

	public CertificateController(CertificateService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public CertificateDto create(@Valid @RequestBody CertificateCreateDto request) {
		return service.create(request);
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public List<CertificateDto> findAll() {
		return service.findAll();
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public CertificateDto findById(@PathVariable UUID id) {
		return service.findById(id);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public CertificateDto update(@PathVariable UUID id, @Valid @RequestBody CertificateUpdateDto request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public void delete(@PathVariable UUID id) {
		service.delete(id);
	}
}
