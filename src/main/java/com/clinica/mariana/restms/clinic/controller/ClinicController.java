package com.clinica.mariana.restms.clinic.controller;

import com.clinica.mariana.restms.clinic.dto.ClinicCreateDto;
import com.clinica.mariana.restms.clinic.dto.ClinicDto;
import com.clinica.mariana.restms.clinic.dto.ClinicUpdateDto;
import com.clinica.mariana.restms.clinic.service.ClinicService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clinics")
public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @PostMapping
    public ResponseEntity<ClinicDto> create(@Valid @RequestBody ClinicCreateDto request) {
        ClinicDto created = clinicService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ClinicDto>> findAll() {
        List<ClinicDto> clinics = clinicService.findAll();
        return ResponseEntity.ok(clinics);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<ClinicDto>> findAllPaged(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ClinicDto> page = clinicService.findAllPaged(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicDto> findById(@PathVariable UUID id) {
        ClinicDto clinic = clinicService.findById(id);
        return ResponseEntity.ok(clinic);
    }

    @GetMapping("/document/{document}")
    public ResponseEntity<ClinicDto> findByDocument(@PathVariable String document) {
        ClinicDto clinic = clinicService.findByDocument(document);
        return ResponseEntity.ok(clinic);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody ClinicUpdateDto request) {
        ClinicDto updated = clinicService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/inactivate")
    public ResponseEntity<Void> inactivate(@PathVariable UUID id) {
        clinicService.inactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clinicService.delete(id);
        return ResponseEntity.noContent().build();
    }
}