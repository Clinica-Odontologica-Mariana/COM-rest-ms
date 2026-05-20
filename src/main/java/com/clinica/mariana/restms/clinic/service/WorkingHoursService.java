package com.clinica.mariana.restms.clinic.service;

import com.clinica.mariana.restms.clinic.dto.WorkingHoursCreateDto;
import com.clinica.mariana.restms.clinic.dto.WorkingHoursDto;
import com.clinica.mariana.restms.clinic.dto.WorkingHoursUpdateDto;
import com.clinica.mariana.restms.clinic.entity.WorkingHoursEntity;
import com.clinica.mariana.restms.clinic.model.WorkingHoursModel;
import com.clinica.mariana.restms.clinic.repository.WorkingHoursRepository;
import com.clinica.mariana.restms.common.exception.AppException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class WorkingHoursService {

    private final WorkingHoursRepository workingHoursRepository;

    public WorkingHoursService(WorkingHoursRepository workingHoursRepository) {
        this.workingHoursRepository = workingHoursRepository;
    }

    @Transactional
    public WorkingHoursDto create(WorkingHoursCreateDto request) {
        if (workingHoursRepository.existsByClinicIdAndDayOfWeek(request.clinicId(), request.dayOfWeek())) {
            throw new AppException(HttpStatus.CONFLICT, "WORKING_HOURS_DAY_CONFLICT",
                    "Working hours for this clinic and day of week already exist");
        }

        WorkingHoursModel model = WorkingHoursModel.create(
                request.clinicId(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        return toDto(toModel(workingHoursRepository.save(toEntity(model))));
    }

    @Transactional(readOnly = true)
    public List<WorkingHoursDto> findByClinicId(UUID clinicId) {
        return workingHoursRepository
                .findAllByClinicIdOrderByDayOfWeekAscStartTimeAsc(clinicId)
                .stream()
                .map(this::toModel)
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkingHoursDto findById(UUID id) {
        WorkingHoursEntity entity = workingHoursRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "WORKING_HOURS_NOT_FOUND", "Working hours not found"));
        return toDto(toModel(entity));
    }

    @Transactional
    public WorkingHoursDto update(UUID id, WorkingHoursUpdateDto request) {
        WorkingHoursEntity entity = workingHoursRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "WORKING_HOURS_NOT_FOUND", "Working hours not found"));

        if (workingHoursRepository.existsByClinicIdAndDayOfWeekAndIdNot(
                entity.getClinicId(), request.dayOfWeek(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "WORKING_HOURS_DAY_CONFLICT",
                    "Working hours for this clinic and day of week already exist");
        }

        WorkingHoursModel model = new WorkingHoursModel(
                id,
                entity.getClinicId(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        apply(entity, model);
        return toDto(toModel(workingHoursRepository.save(entity)));
    }

    @Transactional
    public void delete(UUID id) {
        if (!workingHoursRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "WORKING_HOURS_NOT_FOUND", "Working hours not found");
        }
        workingHoursRepository.deleteById(id);
    }

    private WorkingHoursEntity toEntity(WorkingHoursModel model) {
        WorkingHoursEntity entity = new WorkingHoursEntity();
        apply(entity, model);
        return entity;
    }

    private void apply(WorkingHoursEntity entity, WorkingHoursModel model) {
        entity.setClinicId(model.clinicId());
        entity.setDayOfWeek(model.dayOfWeek());
        entity.setStartTime(model.startTime());
        entity.setEndTime(model.endTime());
    }

    private WorkingHoursModel toModel(WorkingHoursEntity entity) {
        return new WorkingHoursModel(
                entity.getId(),
                entity.getClinicId(),
                entity.getDayOfWeek(),
                entity.getStartTime(),
                entity.getEndTime()
        );
    }

    private WorkingHoursDto toDto(WorkingHoursModel model) {
        return new WorkingHoursDto(
                model.id(),
                model.clinicId(),
                model.dayOfWeek(),
                model.startTime(),
                model.endTime()
        );
    }
}