package com.clinica.mariana.restms.clinic.controller;

import com.clinica.mariana.restms.clinic.dto.EquipmentCreateDto;
import com.clinica.mariana.restms.clinic.dto.EquipmentDto;
import com.clinica.mariana.restms.clinic.dto.EquipmentUpdateDto;
import com.clinica.mariana.restms.clinic.service.EquipmentService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping
    public ResponseEntity<EquipmentDto> create(@Valid @RequestBody EquipmentCreateDto request) {
        EquipmentDto created = equipmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<EquipmentDto>> findByClinicId(
            @RequestParam UUID clinicId,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<EquipmentDto> equipment = equipmentService.findByClinicId(clinicId, activeOnly);
        return ResponseEntity.ok(equipment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDto> findById(@PathVariable UUID id) {
        EquipmentDto equipment = equipmentService.findById(id);
        return ResponseEntity.ok(equipment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody EquipmentUpdateDto request) {
        EquipmentDto updated = equipmentService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/inactivate")
    public ResponseEntity<Void> inactivate(@PathVariable UUID id) {
        equipmentService.inactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build(); 
    }
}