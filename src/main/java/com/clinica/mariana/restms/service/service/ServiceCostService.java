package com.clinica.mariana.restms.service.service;

import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.service.dto.ServiceCostCreateDto;
import com.clinica.mariana.restms.service.dto.ServiceCostDto;
import com.clinica.mariana.restms.service.entity.ServiceCostEntity;
import com.clinica.mariana.restms.service.entity.ServiceCostTypeEntity;
import com.clinica.mariana.restms.service.entity.ServiceEntity;
import com.clinica.mariana.restms.service.model.ServiceCostModel;
import com.clinica.mariana.restms.service.repository.ServiceCostRepository;
import com.clinica.mariana.restms.service.repository.ServiceCostTypeRepository;
import com.clinica.mariana.restms.service.repository.ServiceRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceCostService {

	private final ServiceCostRepository serviceCostRepository;
	private final ServiceRepository serviceRepository;
	private final ServiceCostTypeRepository serviceCostTypeRepository;

	public ServiceCostService(ServiceCostRepository serviceCostRepository, ServiceRepository serviceRepository,
			ServiceCostTypeRepository serviceCostTypeRepository) {
		this.serviceCostRepository = serviceCostRepository;
		this.serviceRepository = serviceRepository;
		this.serviceCostTypeRepository = serviceCostTypeRepository;
	}

	@Transactional
	public ServiceCostDto create(ServiceCostCreateDto request) {
		ServiceEntity service = serviceRepository.findById(request.serviceId())
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SERVICE_NOT_FOUND", "Service not found"));

		if (!service.isActive()) {
			throw new AppException(HttpStatus.CONFLICT, "SERVICE_INACTIVE", "Cannot add cost to an inactive service");
		}

		ServiceCostTypeEntity costType = serviceCostTypeRepository.findById(request.costTypeId())
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SERVICE_COST_TYPE_NOT_FOUND",
						"Service cost type not found"));

		ServiceCostEntity entity = new ServiceCostEntity();
		entity.setServiceId(request.serviceId());
		entity.setCostType(costType);
		entity.setAmount(request.amount());
		entity.setDescription(request.description());
		entity.setValidTo(request.validTo());

		return toDto(toModel(serviceCostRepository.save(entity)));
	}

	@Transactional(readOnly = true)
	public List<ServiceCostDto> findByServiceId(UUID serviceId) {
		return serviceCostRepository.findAllByServiceIdOrderByValidFromDesc(serviceId).stream().map(this::toModel)
				.map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public ServiceCostDto findById(UUID id) {
		ServiceCostEntity entity = serviceCostRepository.findById(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "SERVICE_COST_NOT_FOUND", "Service cost not found"));
		return toDto(toModel(entity));
	}

	private ServiceCostModel toModel(ServiceCostEntity entity) {
		return new ServiceCostModel(entity.getId(), entity.getServiceId(),
				entity.getCostType() != null ? entity.getCostType().getCode() : null,
				entity.getCostType() != null ? entity.getCostType().getName() : null, entity.getAmount(),
				entity.getDescription(), entity.getValidFrom(), entity.getValidTo());
	}

	private ServiceCostDto toDto(ServiceCostModel model) {
		return new ServiceCostDto(model.id(), model.serviceId(), model.costTypeCode(), model.costTypeName(),
				model.amount(), model.description(), model.validFrom(), model.validTo());
	}
}
