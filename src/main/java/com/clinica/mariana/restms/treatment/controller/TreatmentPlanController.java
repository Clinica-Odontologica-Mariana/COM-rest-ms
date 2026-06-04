package com.clinica.mariana.restms.treatment.controller;

import com.clinica.mariana.restms.treatment.dto.TreatmentPlanCreateDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemCreateDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemUpdateDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanUpdateDto;
import com.clinica.mariana.restms.treatment.service.TreatmentPlanService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
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
@RequestMapping("/treatment-plans")
public class TreatmentPlanController {

	private final TreatmentPlanService service;

	public TreatmentPlanController(TreatmentPlanService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public TreatmentPlanDto create(@Valid @RequestBody TreatmentPlanCreateDto request) {
		return service.create(request);
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public TreatmentPlanDto findById(@PathVariable UUID id) {
		return service.findById(id);
	}

	@GetMapping("/by-patient/{patientId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public List<TreatmentPlanDto> findByPatient(@PathVariable UUID patientId) {
		return service.findByPatient(patientId);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public TreatmentPlanDto update(@PathVariable UUID id, @Valid @RequestBody TreatmentPlanUpdateDto request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed("ADMIN")
	public void delete(@PathVariable UUID id) {
		service.delete(id);
	}

	@PostMapping("/{planId}/items")
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public TreatmentPlanItemDto addItem(@PathVariable UUID planId,
			@Valid @RequestBody TreatmentPlanItemCreateDto request) {
		return service.addItem(planId, request);
	}

	@GetMapping("/{planId}/items")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public List<TreatmentPlanItemDto> findItems(@PathVariable UUID planId) {
		return service.findItems(planId);
	}

	@PutMapping("/items/{itemId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public TreatmentPlanItemDto updateItem(@PathVariable UUID itemId,
			@Valid @RequestBody TreatmentPlanItemUpdateDto request) {
		return service.updateItem(itemId, request);
	}

	@PatchMapping("/items/{itemId}/complete")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public TreatmentPlanItemDto completeItem(@PathVariable UUID itemId) {
		return service.completeItem(itemId);
	}

	@DeleteMapping("/items/{itemId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public void deleteItem(@PathVariable UUID itemId) {
		service.deleteItem(itemId);
	}
}
