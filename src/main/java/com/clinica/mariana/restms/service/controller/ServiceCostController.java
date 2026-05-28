package com.clinica.mariana.restms.service.controller;

import com.clinica.mariana.restms.common.api.ApiResponse;
import com.clinica.mariana.restms.service.dto.ServiceCostCreateDto;
import com.clinica.mariana.restms.service.dto.ServiceCostDto;
import com.clinica.mariana.restms.service.service.ServiceCostService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/service-costs")
public class ServiceCostController {

	private final ServiceCostService serviceCostService;

	public ServiceCostController(ServiceCostService serviceCostService) {
		this.serviceCostService = serviceCostService;
	}

	@PostMapping
	@RolesAllowed("ADMIN")
	public ResponseEntity<ApiResponse<ServiceCostDto>> create(@Valid @RequestBody ServiceCostCreateDto request) {
		ServiceCostDto created = serviceCostService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
	}

	@GetMapping
	@RolesAllowed("ADMIN")
	public ResponseEntity<ApiResponse<List<ServiceCostDto>>> findByServiceId(@RequestParam UUID serviceId) {
		return ResponseEntity.ok(ApiResponse.success(serviceCostService.findByServiceId(serviceId)));
	}

	@GetMapping("/{id}")
	@RolesAllowed("ADMIN")
	public ResponseEntity<ApiResponse<ServiceCostDto>> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(ApiResponse.success(serviceCostService.findById(id)));
	}

}
