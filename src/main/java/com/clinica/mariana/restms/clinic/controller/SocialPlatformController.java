package com.clinica.mariana.restms.clinic.controller;

import com.clinica.mariana.restms.clinic.dto.SocialPlatformDto;
import com.clinica.mariana.restms.clinic.service.SocialPlatformService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social-platforms")
public class SocialPlatformController {

    private final SocialPlatformService socialPlatformService;

    public SocialPlatformController(SocialPlatformService socialPlatformService) {
        this.socialPlatformService = socialPlatformService;
    }

    @GetMapping
    public ResponseEntity<List<SocialPlatformDto>> findAll() {
        List<SocialPlatformDto> platforms = socialPlatformService.findAll();
        return ResponseEntity.ok(platforms); // 200
    }

    @GetMapping("/{id}")
    public ResponseEntity<SocialPlatformDto> findById(@PathVariable UUID id) {
        SocialPlatformDto platform = socialPlatformService.findById(id);
        return ResponseEntity.ok(platform); // 200
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<SocialPlatformDto> findByCode(@PathVariable String code) {
        SocialPlatformDto platform = socialPlatformService.findByCode(code);
        return ResponseEntity.ok(platform); // 200
    }
}