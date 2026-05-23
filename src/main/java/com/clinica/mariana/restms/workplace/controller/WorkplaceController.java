package com.clinica.mariana.restms.workplace.controller;

import com.clinica.mariana.restms.workplace.dto.WorkplaceCreateDto;
import com.clinica.mariana.restms.workplace.dto.WorkplaceDto;
import com.clinica.mariana.restms.workplace.dto.WorkplaceUpdateDto;
import com.clinica.mariana.restms.workplace.service.WorkplaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/workplaces")
public class WorkplaceController {

	private final WorkplaceService workplaceService;

	public WorkplaceController(WorkplaceService workplaceService) {
		this.workplaceService = workplaceService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WorkplaceDto create(@Valid @RequestBody WorkplaceCreateDto request) {
		return workplaceService.create(request);
	}

	@GetMapping
	public List<WorkplaceDto> list(@RequestParam UUID clinicId) {
		return workplaceService.findAllByClinic(clinicId);
	}

	@GetMapping("/{id}")
	public WorkplaceDto findById(@PathVariable UUID id) {
		return workplaceService.findById(id);
	}

	@GetMapping("/clinic/{clinicId}")
	public List<WorkplaceDto> findAllByClinic(@PathVariable UUID clinicId) {
		return workplaceService.findAllByClinic(clinicId);
	}

	@PutMapping("/{id}")
	public WorkplaceDto update(@PathVariable UUID id, @Valid @RequestBody WorkplaceUpdateDto request) {
		return workplaceService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		workplaceService.delete(id);
	}
}
