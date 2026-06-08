package com.clinica.mariana.restms.inventory.controller;

import com.clinica.mariana.restms.inventory.dto.InventoryItemCreateDto;
import com.clinica.mariana.restms.inventory.dto.InventoryItemDto;
import com.clinica.mariana.restms.inventory.dto.InventoryItemUpdateDto;
import com.clinica.mariana.restms.inventory.service.InventoryService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory-items")
public class InventoryItemController {

	private final InventoryService service;

	public InventoryItemController(InventoryService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public InventoryItemDto create(@Valid @RequestBody InventoryItemCreateDto request) {
		return service.createItem(request);
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public List<InventoryItemDto> findByClinic(@RequestParam UUID clinicId) {
		return service.findItemsByClinic(clinicId);
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
	public InventoryItemDto findById(@PathVariable UUID id) {
		return service.findItemById(id);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public InventoryItemDto update(@PathVariable UUID id, @Valid @RequestBody InventoryItemUpdateDto request) {
		return service.updateItem(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed("ADMIN")
	public void delete(@PathVariable UUID id) {
		service.deleteItem(id);
	}
}
