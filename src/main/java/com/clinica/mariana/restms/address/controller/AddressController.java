package com.clinica.mariana.restms.address.controller;

import com.clinica.mariana.restms.address.dto.AddressCreateDto;
import com.clinica.mariana.restms.address.dto.AddressDto;
import com.clinica.mariana.restms.address.dto.AddressUpdateDto;
import com.clinica.mariana.restms.address.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/addresses")
public class AddressController {

	private final AddressService addressService;

	public AddressController(AddressService addressService) {
		this.addressService = addressService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AddressDto create(@Valid @RequestBody AddressCreateDto request) {
		return addressService.create(request);
	}

	@GetMapping
	public List<AddressDto> findAll() {
		return addressService.findAll();
	}

	@GetMapping("/{id}")
	public AddressDto findById(@PathVariable UUID id) {
		return addressService.findById(id);
	}

	@PutMapping("/{id}")
	public AddressDto update(@PathVariable UUID id, @Valid @RequestBody AddressUpdateDto request) {
		return addressService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		addressService.delete(id);
	}
}
