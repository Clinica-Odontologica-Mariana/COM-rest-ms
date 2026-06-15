package com.clinica.mariana.restms.clinic.controller;

import com.clinica.mariana.restms.clinic.dto.SocialPlatformDto;
import com.clinica.mariana.restms.clinic.service.SocialPlatformService;
import com.clinica.mariana.restms.common.api.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/social-platforms")
public class SocialPlatformController {

	private final SocialPlatformService socialPlatformService;

	public SocialPlatformController(SocialPlatformService socialPlatformService) {
		this.socialPlatformService = socialPlatformService;
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<List<SocialPlatformDto>>> findAll() {
		List<SocialPlatformDto> platforms = socialPlatformService.findAll();
		return ResponseEntity.ok(ApiResponse.success(platforms));
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<SocialPlatformDto>> findById(@PathVariable UUID id) {
		SocialPlatformDto platform = socialPlatformService.findById(id);
		return ResponseEntity.ok(ApiResponse.success(platform));
	}

	@GetMapping("/code/{code}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<SocialPlatformDto>> findByCode(@PathVariable String code) {
		SocialPlatformDto platform = socialPlatformService.findByCode(code);
		return ResponseEntity.ok(ApiResponse.success(platform));
	}
}
