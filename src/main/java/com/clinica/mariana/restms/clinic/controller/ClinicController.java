package com.clinica.mariana.restms.clinic.controller;

import com.clinica.mariana.restms.clinic.dto.ClinicCreateDto;
import com.clinica.mariana.restms.clinic.dto.ClinicDto;
import com.clinica.mariana.restms.clinic.dto.PublicClinicDto;
import com.clinica.mariana.restms.clinic.dto.ClinicUpdateDto;
import com.clinica.mariana.restms.clinic.service.ClinicService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/clinics")
public class ClinicController {

	private final ClinicService clinicService;

	public ClinicController(ClinicService clinicService) {
		this.clinicService = clinicService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ClinicDto create(@Valid @RequestBody ClinicCreateDto request) {
		return clinicService.create(request);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ClinicDto createWithPhoto(@Valid @RequestPart("clinic") ClinicCreateDto request,
			@RequestPart(value = "photo", required = false) MultipartFile photo) {
		return clinicService.create(request, photo);
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public Page<ClinicDto> findAll(@PageableDefault(size = 20) Pageable pageable) {
		return clinicService.findAll(pageable);
	}

	@GetMapping("/public")
	public Page<PublicClinicDto> findAllPublic(@PageableDefault(size = 20) Pageable pageable) {
		return clinicService.findAllPublic(pageable);
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ClinicDto findById(@PathVariable UUID id) {
		return clinicService.findById(id);
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ClinicDto update(@PathVariable UUID id, @Valid @RequestBody ClinicUpdateDto request) {
		return clinicService.update(id, request);
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ClinicDto updateWithPhoto(@PathVariable UUID id, @Valid @RequestPart("clinic") ClinicUpdateDto request,
			@RequestPart(value = "photo", required = false) MultipartFile photo) {
		return clinicService.update(id, request, photo);
	}

	@PostMapping("/{id}/photo")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ClinicDto uploadPhoto(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
		return clinicService.uploadPhoto(id, file);
	}

	@DeleteMapping("/{id}/photo")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	public ClinicDto deletePhoto(@PathVariable UUID id) {
		return clinicService.deletePhoto(id);
	}

	@PatchMapping("/{id}/inactivate")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public ClinicDto inactivate(@PathVariable UUID id) {
		return clinicService.inactivate(id);
	}

	@PatchMapping("/{id}/activate")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public ClinicDto activate(@PathVariable UUID id) {
		return clinicService.activate(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public void delete(@PathVariable UUID id) {
		clinicService.delete(id);
	}
}
