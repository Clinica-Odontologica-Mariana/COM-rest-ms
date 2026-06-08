package com.clinica.mariana.restms.workplace.service;

import com.clinica.mariana.restms.workplace.dto.WorkplaceCreateDto;
import com.clinica.mariana.restms.workplace.dto.WorkplaceDto;
import com.clinica.mariana.restms.workplace.dto.WorkplaceUpdateDto;
import com.clinica.mariana.restms.workplace.entity.WorkplaceEntity;
import com.clinica.mariana.restms.workplace.model.WorkplaceModel;
import com.clinica.mariana.restms.workplace.repository.WorkplaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clinica.mariana.restms.common.exception.AppException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WorkplaceService {
	private static final String WORKPLACE_NOT_FOUND = "Workplace not found";

	private final WorkplaceRepository workplaceRepository;

	public WorkplaceService(WorkplaceRepository workplaceRepository) {
		this.workplaceRepository = workplaceRepository;
	}

	@Transactional
	public WorkplaceDto create(WorkplaceCreateDto request) {
		if (workplaceRepository.existsByClinicIdAndName(request.clinicId(), request.name())) {
			throw new AppException(HttpStatus.CONFLICT, "WORKPLACE_NAME_ALREADY_EXISTS",
					"Workplace name already exists for this clinic");
		}

		WorkplaceModel model = WorkplaceModel.create(request.clinicId(), request.name(), request.description());

		WorkplaceEntity entity = new WorkplaceEntity();
		entity.setClinicId(model.clinicId());
		entity.setName(model.name());
		entity.setDescription(model.description());
		entity.setActive(true);
		entity.setInactivatedAt(null);

		return toDto(workplaceRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public List<WorkplaceDto> findAllByClinic(UUID clinicId) {
		return workplaceRepository.findAllByClinicIdAndActiveTrueOrderByNameAsc(clinicId).stream().map(this::toDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public WorkplaceDto findById(UUID id) {
		WorkplaceEntity entity = workplaceRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "WORKPLACE_NOT_FOUND", WORKPLACE_NOT_FOUND));

		return toDto(entity);
	}

	@Transactional(readOnly = true)
	public WorkplaceDto findByClinicIdAndName(UUID clinicId, String name) {
		WorkplaceEntity entity = workplaceRepository.findByClinicIdAndName(clinicId, name)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "WORKPLACE_NOT_FOUND", WORKPLACE_NOT_FOUND));

		return toDto(entity);
	}

	@Transactional
	public WorkplaceDto update(UUID id, WorkplaceUpdateDto request) {
		WorkplaceEntity entity = workplaceRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "WORKPLACE_NOT_FOUND", WORKPLACE_NOT_FOUND));

		if (workplaceRepository.existsByClinicIdAndNameAndIdNot(entity.getClinicId(), request.name(), id)) {
			throw new AppException(HttpStatus.CONFLICT, "WORKPLACE_NAME_ALREADY_EXISTS",
					"Workplace name already exists for this clinic");
		}

		entity.setName(request.name());
		entity.setDescription(request.description());

		return toDto(workplaceRepository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		WorkplaceEntity entity = workplaceRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "WORKPLACE_NOT_FOUND", WORKPLACE_NOT_FOUND));

		if (!entity.isActive()) {
			return;
		}

		entity.setActive(false);
		entity.setInactivatedAt(OffsetDateTime.now());
		workplaceRepository.save(entity);
	}

	private WorkplaceDto toDto(WorkplaceEntity entity) {
		return new WorkplaceDto(entity.getId(), entity.getClinicId(), entity.getName(), entity.getDescription(),
				entity.isActive());
	}
}
