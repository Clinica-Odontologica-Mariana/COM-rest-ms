package com.clinica.mariana.restms.inventory.controller;

import com.clinica.mariana.restms.inventory.dto.StockMovementCreateDto;
import com.clinica.mariana.restms.inventory.dto.StockMovementDto;
import com.clinica.mariana.restms.inventory.service.InventoryService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stock-movements")
public class StockMovementController {

	private final InventoryService service;

	public StockMovementController(InventoryService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public StockMovementDto create(@Valid @RequestBody StockMovementCreateDto request) {
		return service.createMovement(request);
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "RECEPTIONIST"})
	public List<StockMovementDto> findByItem(@RequestParam UUID itemId) {
		return service.findMovementsByItem(itemId);
	}
}
