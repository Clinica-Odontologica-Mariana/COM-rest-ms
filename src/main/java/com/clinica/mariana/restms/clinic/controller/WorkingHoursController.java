package com.clinica.mariana.restms.clinic.controller;

import com.clinica.mariana.restms.clinic.dto.WorkingHoursCreateDto;
import com.clinica.mariana.restms.clinic.dto.WorkingHoursDto;
import com.clinica.mariana.restms.clinic.dto.WorkingHoursUpdateDto;
import com.clinica.mariana.restms.clinic.service.WorkingHoursService;
import com.clinica.mariana.restms.common.api.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/working-hours")
public class WorkingHoursController {

	private final WorkingHoursService workingHoursService;

	public WorkingHoursController(WorkingHoursService workingHoursService) {
		this.workingHoursService = workingHoursService;
	}

	@PostMapping
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<WorkingHoursDto>> create(@Valid @RequestBody WorkingHoursCreateDto request) {
		WorkingHoursDto created = workingHoursService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<List<WorkingHoursDto>>> findByClinicId(@RequestParam UUID clinicId) {
		List<WorkingHoursDto> hours = workingHoursService.findByClinicId(clinicId);
		return ResponseEntity.ok(ApiResponse.success(hours));
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<WorkingHoursDto>> findById(@PathVariable UUID id) {
		WorkingHoursDto hours = workingHoursService.findById(id);
		return ResponseEntity.ok(ApiResponse.success(hours));
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ResponseEntity<ApiResponse<WorkingHoursDto>> update(@PathVariable UUID id,
			@Valid @RequestBody WorkingHoursUpdateDto request) {
		WorkingHoursDto updated = workingHoursService.update(id, request);
		return ResponseEntity.ok(ApiResponse.success(updated));
	}

	@DeleteMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
		workingHoursService.delete(id);
		return ResponseEntity.ok(ApiResponse.success(null));
	}
}
