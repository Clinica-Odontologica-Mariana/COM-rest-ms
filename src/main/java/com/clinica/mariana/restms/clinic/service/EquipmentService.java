package com.clinica.mariana.restms.clinic.service;

import com.clinica.mariana.restms.clinic.dto.EquipmentCreateDto;
import com.clinica.mariana.restms.clinic.dto.EquipmentDto;
import com.clinica.mariana.restms.clinic.dto.EquipmentUpdateDto;
import com.clinica.mariana.restms.clinic.entity.EquipmentEntity;
import com.clinica.mariana.restms.clinic.model.EquipmentModel;
import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import com.clinica.mariana.restms.clinic.repository.EquipmentRepository;
import com.clinica.mariana.restms.common.exception.AppException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import java.util.List;
import java.util.UUID;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final ClinicRepository clinicRepository;

    public EquipmentService(EquipmentRepository equipmentRepository, ClinicRepository clinicRepository) {
        this.equipmentRepository = equipmentRepository;
        this.clinicRepository = clinicRepository;
    }

    @Transactional
    public EquipmentDto create(EquipmentCreateDto request) {
        if (!clinicRepository.existsById(request.clinicId())) {
            throw new AppException(HttpStatus.NOT_FOUND, "CLINIC_NOT_FOUND", "Clinic not found");
        }

        if (equipmentRepository.existsByClinicIdAndName(request.clinicId(), request.name())) {
            throw new AppException(HttpStatus.CONFLICT, "EQUIPMENT_NAME_CONFLICT",
                    "Equipment with this name already exists in the clinic");
        }

        EquipmentModel model = EquipmentModel.create(
                request.clinicId(),
                request.name(),
                request.description(),
                request.location()
        );
        return toDto(toModel(equipmentRepository.save(toEntity(model))));
    }

    @Transactional(readOnly = true)
    public List<EquipmentDto> findByClinicId(UUID clinicId, boolean activeOnly) {
        List<EquipmentEntity> entities = activeOnly
                ? equipmentRepository.findAllByClinicIdAndActiveTrueOrderByNameAsc(clinicId)
                : equipmentRepository.findAllByClinicIdOrderByNameAsc(clinicId);
        return entities.stream().map(this::toModel).map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public EquipmentDto findById(UUID id) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "EQUIPMENT_NOT_FOUND", "Equipment not found"));
        return toDto(toModel(entity));
    }

    @Transactional
    public EquipmentDto update(UUID id, EquipmentUpdateDto request) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "EQUIPMENT_NOT_FOUND", "Equipment not found"));

        if (equipmentRepository.existsByClinicIdAndNameAndIdNot(entity.getClinicId(), request.name(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "EQUIPMENT_NAME_CONFLICT",
                    "Equipment with this name already exists in the clinic");
        }

        EquipmentModel model = new EquipmentModel(
                id,
                entity.getClinicId(),
                request.name(),
                request.description(),
                request.location(),
                entity.isActive()
        );

        apply(entity, model);
        return toDto(toModel(equipmentRepository.save(entity)));
    }

    @Transactional
    public void inactivate(UUID id) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "EQUIPMENT_NOT_FOUND", "Equipment not found"));

        if (!entity.isActive()) {
            throw new AppException(HttpStatus.CONFLICT, "EQUIPMENT_ALREADY_INACTIVE", "Equipment is already inactive");
        }

        entity.setActive(false);
        entity.setInactivatedAt(OffsetDateTime.now());
        equipmentRepository.save(entity);
    }

    private EquipmentEntity toEntity(EquipmentModel model) {
        EquipmentEntity entity = new EquipmentEntity();
        apply(entity, model);
        return entity;
    }

    private void apply(EquipmentEntity entity, EquipmentModel model) {
        entity.setClinicId(model.clinicId());
        entity.setName(model.name());
        entity.setDescription(model.description());
        entity.setLocation(model.location());
        entity.setActive(model.active());
    }

    private EquipmentModel toModel(EquipmentEntity entity) {
        return new EquipmentModel(
                entity.getId(),
                entity.getClinicId(),
                entity.getName(),
                entity.getDescription(),
                entity.getLocation(),
                entity.isActive()
        );
    }

    private EquipmentDto toDto(EquipmentModel model) {
        return new EquipmentDto(
                model.id(),
                model.clinicId(),
                model.name(),
                model.description(),
                model.location(),
                model.active()
        );
    }
}