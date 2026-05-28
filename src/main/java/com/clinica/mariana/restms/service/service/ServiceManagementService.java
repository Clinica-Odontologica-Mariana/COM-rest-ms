package com.clinica.mariana.restms.service.service;

import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.service.dto.ServiceCreateDto;
import com.clinica.mariana.restms.service.dto.ServiceDto;
import com.clinica.mariana.restms.service.dto.ServiceUpdateDto;
import com.clinica.mariana.restms.service.entity.ServiceCategoryEntity;
import com.clinica.mariana.restms.service.entity.ServiceEntity;
import com.clinica.mariana.restms.service.model.ServiceModel;
import com.clinica.mariana.restms.service.repository.ServiceCategoryRepository;
import com.clinica.mariana.restms.service.repository.ServiceRepository;
import com.clinica.mariana.restms.users.repository.AppUserReferenceRepository;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ServiceManagementService {

	private final ServiceRepository serviceRepository;
	private final ServiceCategoryRepository serviceCategoryRepository;
	private final AppUserReferenceRepository appUserReferenceRepository;


	public ServiceManagementService(ServiceRepository serviceRepository,
			ServiceCategoryRepository serviceCategoryRepository, AppUserReferenceRepository appUserReferenceRepository) {
		this.serviceRepository = serviceRepository;
		this.serviceCategoryRepository = serviceCategoryRepository;
		this.appUserReferenceRepository = appUserReferenceRepository;
	}

	@Transactional
	public ServiceDto create(ServiceCreateDto request, String keycloakSubject) {
		ServiceCategoryEntity category = serviceCategoryRepository.findById(request.categoryId())
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SERVICE_CATEGORY_NOT_FOUND",
						"Service category not found"));

		if (serviceRepository.existsByNameIgnoreCaseAndCategoryId(request.name(), request.categoryId())) {
			throw new AppException(HttpStatus.CONFLICT, "SERVICE_NAME_CONFLICT",
					"A service with this name already exists in the category");
		}

		UUID createdByUserId = null;
		if (keycloakSubject != null) {
			createdByUserId = appUserReferenceRepository
					.findActiveByKeycloakSubject(keycloakSubject)
					.map(AppUserReferenceRepository.AppUserReference::id)
					.orElse(null);
		}

		ServiceEntity entity = new ServiceEntity();
		entity.setCategory(category);
		entity.setCreatedByUserId(createdByUserId);
		entity.setName(request.name());
		entity.setDescription(request.description());
		entity.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
		entity.setActive(true);

		return toDto(toModel(serviceRepository.save(entity)));
	}

	@Transactional(readOnly = true)
	public List<ServiceDto> findAll(boolean activeOnly) {
		List<ServiceEntity> entities = activeOnly
				? serviceRepository.findAllByActiveTrueOrderByNameAsc()
				: serviceRepository.findAll();
		return entities.stream().map(this::toModel).map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public List<ServiceDto> findByCategoryId(UUID categoryId, boolean activeOnly) {
		List<ServiceEntity> entities = activeOnly
				? serviceRepository.findAllByCategoryIdAndActiveTrueOrderByNameAsc(categoryId)
				: serviceRepository.findAllByCategoryIdOrderByNameAsc(categoryId);
		return entities.stream().map(this::toModel).map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public ServiceDto findById(UUID id) {
		ServiceEntity entity = serviceRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SERVICE_NOT_FOUND", "Service not found"));
		return toDto(toModel(entity));
	}

	@Transactional
	public ServiceDto update(UUID id, ServiceUpdateDto request) {
		ServiceEntity entity = serviceRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SERVICE_NOT_FOUND", "Service not found"));

		ServiceCategoryEntity category = serviceCategoryRepository.findById(request.categoryId())
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SERVICE_CATEGORY_NOT_FOUND",
						"Service category not found"));

		if (serviceRepository.existsByNameIgnoreCaseAndCategoryIdAndIdNot(request.name(), request.categoryId(), id)) {
			throw new AppException(HttpStatus.CONFLICT, "SERVICE_NAME_CONFLICT",
					"A service with this name already exists in the category");
		}

		entity.setCategory(category);
		entity.setName(request.name());
		entity.setDescription(request.description());
		entity.setEstimatedDurationMinutes(request.estimatedDurationMinutes());

		return toDto(toModel(serviceRepository.save(entity)));
	}

	@Transactional
	public void inactivate(UUID id) {
		ServiceEntity entity = serviceRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SERVICE_NOT_FOUND", "Service not found"));

		if (!entity.isActive()) {
			throw new AppException(HttpStatus.CONFLICT, "SERVICE_ALREADY_INACTIVE", "Service is already inactive");
		}

		entity.setActive(false);
		entity.setInactivatedAt(OffsetDateTime.now());
		serviceRepository.save(entity);
	}

	private ServiceModel toModel(ServiceEntity entity) {
		return new ServiceModel(entity.getId(), entity.getCategory() != null ? entity.getCategory().getCode() : null,
				entity.getCategory() != null ? entity.getCategory().getName() : null, entity.getCreatedByUserId(),
				entity.getName(), entity.getDescription(), entity.getEstimatedDurationMinutes(), entity.isActive());
	}

	private ServiceDto toDto(ServiceModel model) {
		return new ServiceDto(model.id(), model.categoryCode(), model.categoryName(), model.name(), model.description(),
				model.estimatedDurationMinutes(), model.active());
	}
}
