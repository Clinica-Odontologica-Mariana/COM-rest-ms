package com.clinica.mariana.restms.service.service;

import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.service.dto.ServiceCategoryDto;
import com.clinica.mariana.restms.service.entity.ServiceCategoryEntity;
import com.clinica.mariana.restms.service.repository.ServiceCategoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceCategoryService {

	private final ServiceCategoryRepository serviceCategoryRepository;

	public ServiceCategoryService(ServiceCategoryRepository serviceCategoryRepository) {
		this.serviceCategoryRepository = serviceCategoryRepository;
	}

	@Transactional(readOnly = true)
	public List<ServiceCategoryDto> findAll() {
		return serviceCategoryRepository.findAllByOrderByNameAsc().stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public ServiceCategoryDto findById(UUID id) {
		ServiceCategoryEntity entity = serviceCategoryRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SERVICE_CATEGORY_NOT_FOUND",
						"Service category not found"));
		return toDto(entity);
	}

	private ServiceCategoryDto toDto(ServiceCategoryEntity entity) {
		return new ServiceCategoryDto(entity.getId(), entity.getCode(), entity.getName());
	}
}
