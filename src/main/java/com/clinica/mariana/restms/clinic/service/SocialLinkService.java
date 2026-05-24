package com.clinica.mariana.restms.clinic.service;

import com.clinica.mariana.restms.clinic.dto.SocialLinkCreateDto;
import com.clinica.mariana.restms.clinic.dto.SocialLinkDto;
import com.clinica.mariana.restms.clinic.dto.SocialLinkUpdateDto;
import com.clinica.mariana.restms.clinic.entity.SocialLinkEntity;
import com.clinica.mariana.restms.clinic.model.SocialLinkModel;
import com.clinica.mariana.restms.clinic.repository.SocialLinkRepository;
import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import com.clinica.mariana.restms.clinic.repository.SocialPlatformRepository;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SocialLinkService {

    private final SocialLinkRepository socialLinkRepository;
    private final ClinicRepository clinicRepository;
    private final SocialPlatformRepository socialPlatformRepository;

    public SocialLinkService(SocialLinkRepository socialLinkRepository,
                             ClinicRepository clinicRepository,
                             SocialPlatformRepository socialPlatformRepository) {
        this.socialLinkRepository = socialLinkRepository;
        this.clinicRepository = clinicRepository;
        this.socialPlatformRepository = socialPlatformRepository;
    }

    public SocialLinkDto create(SocialLinkCreateDto request) {
        if (!clinicRepository.existsById(request.clinicId())) {
            throw new AppException(HttpStatus.NOT_FOUND, "CLINIC_NOT_FOUND", "Clinic not found");
        }

        if (!socialPlatformRepository.existsById(request.platformId())) {
            throw new AppException(HttpStatus.NOT_FOUND, "SOCIAL_PLATFORM_NOT_FOUND", "Social platform not found");
        }

        if (socialLinkRepository.existsByClinicIdAndPlatformId(request.clinicId(), request.platformId())) {
            throw new AppException(HttpStatus.CONFLICT, "SOCIAL_LINK_PLATFORM_CONFLICT",
                    "A social link for this clinic and platform already exists");
        }

        SocialLinkModel model = SocialLinkModel.create(
                request.clinicId(),
                request.platformId(),
                request.url()
        );

        return toDto(toModel(socialLinkRepository.save(toEntity(model))));
    }

    @Transactional(readOnly = true)
    public List<SocialLinkDto> findByClinicId(UUID clinicId) {
        return socialLinkRepository.findAllByClinicIdOrderByCreatedAtAsc(clinicId)
                .stream()
                .map(this::toModel)
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SocialLinkDto findById(UUID id) {
        SocialLinkEntity entity = socialLinkRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SOCIAL_LINK_NOT_FOUND", "Social link not found"));
        return toDto(toModel(entity));
    }

    @Transactional
    public SocialLinkDto update(UUID id, SocialLinkUpdateDto request) {
        SocialLinkEntity entity = socialLinkRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SOCIAL_LINK_NOT_FOUND", "Social link not found"));

        if (!socialPlatformRepository.existsById(request.platformId())) {
            throw new AppException(HttpStatus.NOT_FOUND, "SOCIAL_PLATFORM_NOT_FOUND", "Social platform not found");
        }

        if (socialLinkRepository.existsByClinicIdAndPlatformIdAndIdNot(
                entity.getClinicId(), request.platformId(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "SOCIAL_LINK_PLATFORM_CONFLICT",
                    "A social link for this clinic and platform already exists");
        }

        SocialLinkModel model = new SocialLinkModel(
                id,
                entity.getClinicId(),
                request.platformId(),
                request.url()
        );

        apply(entity, model);
        return toDto(toModel(socialLinkRepository.save(entity)));
    }

    @Transactional
    public void delete(UUID id) {
        if (!socialLinkRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "SOCIAL_LINK_NOT_FOUND", "Social link not found");
        }
        socialLinkRepository.deleteById(id);
    }

    private SocialLinkEntity toEntity(SocialLinkModel model) {
        SocialLinkEntity entity = new SocialLinkEntity();
        apply(entity, model);
        return entity;
    }

    private void apply(SocialLinkEntity entity, SocialLinkModel model) {
        entity.setClinicId(model.clinicId());
        entity.setPlatformId(model.platformId());
        entity.setUrl(model.url());
    }

    private SocialLinkModel toModel(SocialLinkEntity entity) {
        return new SocialLinkModel(
                entity.getId(),
                entity.getClinicId(),
                entity.getPlatformId(),
                entity.getUrl()
        );
    }

    private SocialLinkDto toDto(SocialLinkModel model) {
        return new SocialLinkDto(
                model.id(),
                model.clinicId(),
                model.platformId(),
                model.url()
        );
    }
}