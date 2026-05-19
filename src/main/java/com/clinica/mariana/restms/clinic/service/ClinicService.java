package com.clinica.mariana.restms.clinic.service;

import com.clinica.mariana.restms.clinic.dto.ClinicCreateDto;
import com.clinica.mariana.restms.clinic.dto.ClinicDto;
import com.clinica.mariana.restms.clinic.dto.ClinicUpdateDto;
import com.clinica.mariana.restms.clinic.entity.ClinicEntity;
import com.clinica.mariana.restms.clinic.model.ClinicModel;
import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ClinicService {

    private final ClinicRepository clinicRepository;

    public ClinicService(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    @Transactional
    public ClinicDto create(ClinicCreateDto request) {
        if (clinicRepository.existsByDocument(request.document())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Clinic document already exists");
        }

        ClinicModel model = ClinicModel.create(
                request.addressId(),
                request.name(),
                request.document(),
                request.phone(),
                request.email(),
                request.timezone()
        );

        return toDto(toModel(clinicRepository.save(toEntity(model))));
    }

    @Transactional(readOnly = true)
    public List<ClinicDto> findAll() {
        return clinicRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toModel)
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ClinicDto> findAllPaged(Pageable pageable) {
        return clinicRepository.findAll(pageable)
                .map(this::toModel)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ClinicDto findById(UUID id) {
        ClinicEntity entity = clinicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

        return toDto(toModel(entity));
    }

    @Transactional(readOnly = true)
    public ClinicDto findByDocument(String document) {
        ClinicEntity entity = clinicRepository.findByDocument(document)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

        return toDto(toModel(entity));
    }

    @Transactional
    public ClinicDto update(UUID id, ClinicUpdateDto request) {
        ClinicEntity entity = clinicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

        if (clinicRepository.existsByDocumentAndIdNot(request.document(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Clinic document already exists");
        }

        ClinicModel model = new ClinicModel(
                id,
                request.addressId(),
                request.name(),
                request.document(),
                request.phone(),
                request.email(),
                request.timezone() != null ? request.timezone() : entity.getTimezone(),
                entity.isActive()
        );

        apply(entity, model);

        return toDto(toModel(clinicRepository.save(entity)));
    }

    @Transactional
    public void inactivate(UUID id) {
        ClinicEntity entity = clinicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

        if (!entity.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Clinic is already inactive");
        }

        entity.setActive(false);
        entity.setInactivatedAt(OffsetDateTime.now());
        clinicRepository.save(entity);
    }

    @Transactional
    public void delete(UUID id) {
        if (!clinicRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found");
        }
        clinicRepository.deleteById(id);
    }

    // ── mapping helpers ──────────────────────────────────────────────────────

    private ClinicEntity toEntity(ClinicModel model) {
        ClinicEntity entity = new ClinicEntity();
        apply(entity, model);
        return entity;
    }

    private void apply(ClinicEntity entity, ClinicModel model) {
        entity.setAddressId(model.addressId());
        entity.setName(model.name());
        entity.setDocument(model.document());
        entity.setPhone(model.phone());
        entity.setEmail(model.email());
        entity.setTimezone(model.timezone());
        entity.setActive(model.active());
    }

    private ClinicModel toModel(ClinicEntity entity) {
        return new ClinicModel(
                entity.getId(),
                entity.getAddressId(),
                entity.getName(),
                entity.getDocument(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getTimezone(),
                entity.isActive()
        );
    }

    private ClinicDto toDto(ClinicModel model) {
        return new ClinicDto(
                model.id(),
                model.addressId(),
                model.name(),
                model.document(),
                model.phone(),
                model.email(),
                model.timezone(),
                model.active()
        );
    }
}