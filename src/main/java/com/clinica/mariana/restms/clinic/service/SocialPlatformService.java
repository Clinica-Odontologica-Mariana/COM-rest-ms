package com.clinica.mariana.restms.clinic.service;

import com.clinica.mariana.restms.clinic.dto.SocialPlatformDto;
import com.clinica.mariana.restms.clinic.entity.SocialPlatformEntity;
import com.clinica.mariana.restms.clinic.repository.SocialPlatformRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class SocialPlatformService {

    private final SocialPlatformRepository socialPlatformRepository;

    public SocialPlatformService(SocialPlatformRepository socialPlatformRepository) {
        this.socialPlatformRepository = socialPlatformRepository;
    }

    @Transactional(readOnly = true)
    public List<SocialPlatformDto> findAll() {
        return socialPlatformRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SocialPlatformDto findById(UUID id) {
        SocialPlatformEntity entity = socialPlatformRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Social platform not found"));
        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public SocialPlatformDto findByCode(String code) {
        SocialPlatformEntity entity = socialPlatformRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Social platform not found"));
        return toDto(entity);
    }

    private SocialPlatformDto toDto(SocialPlatformEntity entity) {
        return new SocialPlatformDto(entity.getId(), entity.getCode(), entity.getName());
    }
}