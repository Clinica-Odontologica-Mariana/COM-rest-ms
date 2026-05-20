package com.clinica.mariana.restms.clinic.controller;

import com.clinica.mariana.restms.clinic.dto.ClinicCreateDto;
import com.clinica.mariana.restms.clinic.dto.ClinicDto;
import com.clinica.mariana.restms.clinic.dto.ClinicUpdateDto;
import com.clinica.mariana.restms.clinic.service.ClinicService;
import com.clinica.mariana.restms.common.api.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
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
@RequestMapping("/clinics")
public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @PostMapping
    @RolesAllowed({"ADMIN", "RECEPTIONIST"})
    public ResponseEntity<ApiResponse<ClinicDto>> create(@Valid @RequestBody ClinicCreateDto request) {
        ClinicDto created = clinicService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @GetMapping
    @RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
    public ResponseEntity<ApiResponse<List<ClinicDto>>> findAll() {
        List<ClinicDto> clinics = clinicService.findAll();
        return ResponseEntity.ok(ApiResponse.success(clinics));
    }

    @GetMapping("/paged")
    @RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
    public ResponseEntity<ApiResponse<Page<ClinicDto>>> findAllPaged(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ClinicDto> page = clinicService.findAllPaged(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    @RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
    public ResponseEntity<ApiResponse<ClinicDto>> findById(@PathVariable UUID id) {
        ClinicDto clinic = clinicService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(clinic));
    }

    @GetMapping("/document/{document}")
    @RolesAllowed({"ADMIN", "RECEPTIONIST", "DOCTOR"})
    public ResponseEntity<ApiResponse<ClinicDto>> findByDocument(@PathVariable String document) {
        ClinicDto clinic = clinicService.findByDocument(document);
        return ResponseEntity.ok(ApiResponse.success(clinic));
    }

    @PutMapping("/{id}")
    @RolesAllowed({"ADMIN", "RECEPTIONIST"})
    public ResponseEntity<ApiResponse<ClinicDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ClinicUpdateDto request) {
        ClinicDto updated = clinicService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PatchMapping("/{id}/inactivate")
    @RolesAllowed("ADMIN")
    public ResponseEntity<ApiResponse<Void>> inactivate(@PathVariable UUID id) {
        clinicService.inactivate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @RolesAllowed("ADMIN")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        clinicService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}