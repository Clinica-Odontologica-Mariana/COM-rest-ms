package com.clinica.mariana.restms.certificate.controller;

import com.clinica.mariana.restms.certificate.dto.CertificateCreateDto;
import com.clinica.mariana.restms.certificate.dto.CertificateDto;
import com.clinica.mariana.restms.certificate.dto.CertificateFeaturedRequest;
import com.clinica.mariana.restms.certificate.dto.CertificatePublicDto;
import com.clinica.mariana.restms.certificate.dto.CertificateUpdateDto;
import com.clinica.mariana.restms.certificate.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
	public Page<CertificateDto> findAll(@PageableDefault(size = 20) Pageable pageable) {
		return service.findAll(pageable);
	}

	@GetMapping("/featured")
	public List<CertificatePublicDto> findFeatured() {
		return service.findFeatured();
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public CertificateDto findById(@PathVariable UUID id) {
		return service.findById(id);
	}

	@PatchMapping("/{id}/featured")
	@RolesAllowed("ADMIN")
	@Operation(summary = "Marca ou remove destaque de um certificado")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "Destaque atualizado com sucesso"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Certificado não encontrado"),
			@ApiResponse(responseCode = "422", description = "Limite de destaques atingido (FEATURED_LIMIT_REACHED)")})
	public CertificateDto setFeatured(@PathVariable UUID id, @Valid @RequestBody CertificateFeaturedRequest request) {
		return service.setFeatured(id, request.featured());
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
