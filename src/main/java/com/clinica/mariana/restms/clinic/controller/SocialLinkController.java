package com.clinica.mariana.restms.clinic.controller;

import com.clinica.mariana.restms.clinic.dto.SocialLinkCreateDto;
import com.clinica.mariana.restms.clinic.dto.SocialLinkDto;
import com.clinica.mariana.restms.clinic.dto.SocialLinkUpdateDto;
import com.clinica.mariana.restms.clinic.service.SocialLinkService;
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
@RequestMapping("/api/v1/social-links")
public class SocialLinkController {

    private final SocialLinkService socialLinkService;

    public SocialLinkController(SocialLinkService socialLinkService) {
        this.socialLinkService = socialLinkService;
    }

    @PostMapping
    public ResponseEntity<SocialLinkDto> create(@Valid @RequestBody SocialLinkCreateDto request) {
        SocialLinkDto created = socialLinkService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<SocialLinkDto>> findByClinicId(@RequestParam UUID clinicId) {
        List<SocialLinkDto> links = socialLinkService.findByClinicId(clinicId);
        return ResponseEntity.ok(links);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SocialLinkDto> findById(@PathVariable UUID id) {
        SocialLinkDto link = socialLinkService.findById(id);
        return ResponseEntity.ok(link);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SocialLinkDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody SocialLinkUpdateDto request) {
        SocialLinkDto updated = socialLinkService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        socialLinkService.delete(id);
        return ResponseEntity.noContent().build();
    }
}