package com.clinica.mariana.restms.clinic.controller;

import com.clinica.mariana.restms.clinic.dto.WorkingHoursCreateDto;
import com.clinica.mariana.restms.clinic.dto.WorkingHoursDto;
import com.clinica.mariana.restms.clinic.dto.WorkingHoursUpdateDto;
import com.clinica.mariana.restms.clinic.service.WorkingHoursService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/working-hours")
public class WorkingHoursController {

    private final WorkingHoursService workingHoursService;

    public WorkingHoursController(WorkingHoursService workingHoursService) {
        this.workingHoursService = workingHoursService;
    }

    @PostMapping
    public ResponseEntity<WorkingHoursDto> create(@Valid @RequestBody WorkingHoursCreateDto request) {
        WorkingHoursDto created = workingHoursService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<WorkingHoursDto>> findByClinicId(@RequestParam UUID clinicId) {
        List<WorkingHoursDto> workingHours = workingHoursService.findByClinicId(clinicId);
        return ResponseEntity.ok(workingHours);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkingHoursDto> findById(@PathVariable UUID id) {
        WorkingHoursDto workingHours = workingHoursService.findById(id);
        return ResponseEntity.ok(workingHours);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkingHoursDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody WorkingHoursUpdateDto request) {
        WorkingHoursDto updated = workingHoursService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        workingHoursService.delete(id);
        return ResponseEntity.noContent().build();
    }
}