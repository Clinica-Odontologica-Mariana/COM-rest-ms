package com.clinica.mariana.restms.service.controller;

import com.clinica.mariana.restms.common.api.ApiResponse;
import com.clinica.mariana.restms.service.dto.ServicePriceCreateDto;
import com.clinica.mariana.restms.service.dto.ServicePriceDto;
import com.clinica.mariana.restms.service.service.ServicePriceService;

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
@RequestMapping("/service-prices")
public class ServicePriceController {

	private final ServicePriceService servicePriceService;

	public ServicePriceController(ServicePriceService servicePriceService) {
		this.servicePriceService = servicePriceService;
	}

	@PostMapping
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<ServicePriceDto>> create(@Valid @RequestBody ServicePriceCreateDto request) {
		ServicePriceDto created = servicePriceService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public ResponseEntity<ApiResponse<List<ServicePriceDto>>> findByServiceId(@RequestParam UUID serviceId) {
		return ResponseEntity.ok(ApiResponse.success(servicePriceService.findByServiceId(serviceId)));
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public ResponseEntity<ApiResponse<ServicePriceDto>> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(ApiResponse.success(servicePriceService.findById(id)));
	}
}
