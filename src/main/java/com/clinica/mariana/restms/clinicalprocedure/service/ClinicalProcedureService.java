package com.clinica.mariana.restms.clinicalprocedure.service;

import com.clinica.mariana.restms.clinicalprocedure.dto.ClinicalProcedureCreateDto;
import com.clinica.mariana.restms.clinicalprocedure.dto.ClinicalProcedureDto;
import com.clinica.mariana.restms.clinicalprocedure.dto.ClinicalProcedureUpdateDto;
import com.clinica.mariana.restms.clinicalprocedure.entity.ClinicalProcedureEntity;
import com.clinica.mariana.restms.clinicalprocedure.repository.ClinicalProcedureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ClinicalProcedureService {

	private static final String PROCEDURE_NOT_FOUND = "Clinical procedure not found";

	private final ClinicalProcedureRepository repository;

	public ClinicalProcedureService(ClinicalProcedureRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public ClinicalProcedureDto create(ClinicalProcedureCreateDto request) {
		validateUnique(request.code(), request.name(), null);
		ClinicalProcedureEntity entity = new ClinicalProcedureEntity();
		apply(entity, request.code(), request.name(), request.category(), request.description(),
				request.estimatedDurationMinutes(), request.basePrice());
		entity.setActive(true);
		entity.setInactivatedAt(null);
		return toDto(repository.save(entity));
	}

	@Transactional(readOnly = true)
	public List<ClinicalProcedureDto> findAll() {
		return repository.findAllByActiveTrueOrderByNameAsc().stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public ClinicalProcedureDto findById(UUID id) {
		return toDto(findEntity(id));
	}

	@Transactional
	public ClinicalProcedureDto update(UUID id, ClinicalProcedureUpdateDto request) {
		ClinicalProcedureEntity entity = findEntity(id);
		validateUnique(request.code(), request.name(), id);
		apply(entity, request.code(), request.name(), request.category(), request.description(),
				request.estimatedDurationMinutes(), request.basePrice());
		return toDto(repository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		ClinicalProcedureEntity entity = findEntity(id);
		if (!entity.isActive()) {
			return;
		}
		entity.setActive(false);
		entity.setInactivatedAt(OffsetDateTime.now());
		repository.save(entity);
	}

	private ClinicalProcedureEntity findEntity(UUID id) {
		return repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PROCEDURE_NOT_FOUND));
	}

	private void validateUnique(String code, String name, UUID idToIgnore) {
		if (code != null && !code.isBlank()) {
			boolean codeExists = idToIgnore == null
					? repository.existsByCode(code)
					: repository.existsByCodeAndIdNot(code, idToIgnore);
			if (codeExists) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Clinical procedure code already exists");
			}
		}
		boolean nameExists = idToIgnore == null
				? repository.existsByName(name)
				: repository.existsByNameAndIdNot(name, idToIgnore);
		if (nameExists) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Clinical procedure name already exists");
		}
	}

	private void apply(ClinicalProcedureEntity entity, String code, String name, String category, String description,
			Integer estimatedDurationMinutes, java.math.BigDecimal basePrice) {
		entity.setCode(code == null || code.isBlank() ? null : code);
		entity.setName(name);
		entity.setCategory(category);
		entity.setDescription(description);
		entity.setEstimatedDurationMinutes(estimatedDurationMinutes);
		entity.setBasePrice(basePrice);
	}

	private ClinicalProcedureDto toDto(ClinicalProcedureEntity entity) {
		return new ClinicalProcedureDto(entity.getId(), entity.getCode(), entity.getName(), entity.getCategory(),
				entity.getDescription(), entity.getEstimatedDurationMinutes(), entity.getBasePrice(), entity.isActive(),
				entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
