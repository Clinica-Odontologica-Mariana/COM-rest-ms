package com.clinica.mariana.restms.clinic.controller;

import com.clinica.mariana.restms.clinic.dto.SocialLinkCreateDto;
import com.clinica.mariana.restms.clinic.dto.SocialLinkDto;
import com.clinica.mariana.restms.clinic.dto.SocialLinkUpdateDto;
import com.clinica.mariana.restms.clinic.service.SocialLinkService;
import com.clinica.mariana.restms.common.api.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/social-links")
public class SocialLinkController {

	private final SocialLinkService socialLinkService;

	public SocialLinkController(SocialLinkService socialLinkService) {
		this.socialLinkService = socialLinkService;
	}

	@PostMapping
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<SocialLinkDto>> create(@Valid @RequestBody SocialLinkCreateDto request) {
		SocialLinkDto created = socialLinkService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<List<SocialLinkDto>>> findByClinicId(@RequestParam UUID clinicId) {
		List<SocialLinkDto> links = socialLinkService.findByClinicId(clinicId);
		return ResponseEntity.ok(ApiResponse.success(links));
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<SocialLinkDto>> findById(@PathVariable UUID id) {
		SocialLinkDto link = socialLinkService.findById(id);
		return ResponseEntity.ok(ApiResponse.success(link));
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<SocialLinkDto>> update(@PathVariable UUID id,
			@Valid @RequestBody SocialLinkUpdateDto request) {
		SocialLinkDto updated = socialLinkService.update(id, request);
		return ResponseEntity.ok(ApiResponse.success(updated));
	}

	@DeleteMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
		socialLinkService.delete(id);
		return ResponseEntity.ok(ApiResponse.success(null));
	}
}
