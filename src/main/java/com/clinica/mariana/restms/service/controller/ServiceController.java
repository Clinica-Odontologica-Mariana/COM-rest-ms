package com.clinica.mariana.restms.service.controller;

import com.clinica.mariana.restms.common.api.ApiResponse;
import com.clinica.mariana.restms.service.dto.ServiceCategoryDto;
import com.clinica.mariana.restms.service.dto.ServiceCreateDto;
import com.clinica.mariana.restms.service.dto.ServiceDto;
import com.clinica.mariana.restms.service.dto.ServiceUpdateDto;
import com.clinica.mariana.restms.service.service.ServiceCategoryService;
import com.clinica.mariana.restms.service.service.ServiceManagementService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services")
public class ServiceController {

	private final ServiceManagementService serviceManagementService;
	private final ServiceCategoryService serviceCategoryService;

	public ServiceController(ServiceManagementService serviceManagementService,
			ServiceCategoryService serviceCategoryService) {
		this.serviceManagementService = serviceManagementService;
		this.serviceCategoryService = serviceCategoryService;
	}

	@GetMapping("/categories")
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public ResponseEntity<ApiResponse<List<ServiceCategoryDto>>> findAllCategories() {
		return ResponseEntity.ok(ApiResponse.success(serviceCategoryService.findAll()));
	}

	@PostMapping
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<ServiceDto>> create(@Valid @RequestBody ServiceCreateDto request,
	                                                      @AuthenticationPrincipal Jwt jwt) {
		String subject = jwt != null ? jwt.getSubject() : null;
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(serviceManagementService.create(request, subject)));
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public ResponseEntity<ApiResponse<List<ServiceDto>>> findAll(
			@RequestParam(defaultValue = "true") boolean activeOnly) {
		return ResponseEntity.ok(ApiResponse.success(serviceManagementService.findAll(activeOnly)));
	}

	@GetMapping("/by-category")
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public ResponseEntity<ApiResponse<List<ServiceDto>>> findByCategoryId(@RequestParam UUID categoryId,
			@RequestParam(defaultValue = "true") boolean activeOnly) {
		return ResponseEntity
				.ok(ApiResponse.success(serviceManagementService.findByCategoryId(categoryId, activeOnly)));
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public ResponseEntity<ApiResponse<ServiceDto>> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(ApiResponse.success(serviceManagementService.findById(id)));
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<ServiceDto>> update(@PathVariable UUID id,
			@Valid @RequestBody ServiceUpdateDto request) {
		return ResponseEntity.ok(ApiResponse.success(serviceManagementService.update(id, request)));
	}

	@PatchMapping("/{id}/inactivate")
	@RolesAllowed("ADMIN")
	public ResponseEntity<ApiResponse<Void>> inactivate(@PathVariable UUID id) {
		serviceManagementService.inactivate(id);
		return ResponseEntity.ok(ApiResponse.success(null));
	}
}
