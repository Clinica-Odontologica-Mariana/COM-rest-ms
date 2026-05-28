package com.clinica.mariana.restms.service.service;

import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.service.dto.ServicePriceCreateDto;
import com.clinica.mariana.restms.service.dto.ServicePriceDto;
import com.clinica.mariana.restms.service.entity.ServiceEntity;
import com.clinica.mariana.restms.service.entity.ServicePriceEntity;
import com.clinica.mariana.restms.service.model.ServicePriceModel;
import com.clinica.mariana.restms.service.repository.ServicePriceRepository;
import com.clinica.mariana.restms.service.repository.ServiceRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ServicePriceService {

	private final ServicePriceRepository servicePriceRepository;
	private final ServiceRepository serviceRepository;

	public ServicePriceService(ServicePriceRepository servicePriceRepository, ServiceRepository serviceRepository) {
		this.servicePriceRepository = servicePriceRepository;
		this.serviceRepository = serviceRepository;
	}

	@Transactional
	public ServicePriceDto create(ServicePriceCreateDto request) {
		ServiceEntity service = serviceRepository.findById(request.serviceId())
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SERVICE_NOT_FOUND", "Service not found"));

		if (!service.isActive()) {
			throw new AppException(HttpStatus.CONFLICT, "SERVICE_INACTIVE", "Cannot add price to an inactive service");
		}

		ServicePriceModel model = new ServicePriceModel(null, request.serviceId(), request.amount(),
				request.description(), request.validFrom(), request.validTo());

		return toDto(toModel(servicePriceRepository.save(toEntity(model))));
	}

	@Transactional(readOnly = true)
	public List<ServicePriceDto> findByServiceId(UUID serviceId) {
		return servicePriceRepository.findAllByServiceIdOrderByValidFromDesc(serviceId).stream().map(this::toModel)
				.map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public ServicePriceDto findById(UUID id) {
		ServicePriceEntity entity = servicePriceRepository.findById(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "SERVICE_PRICE_NOT_FOUND", "Service price not found"));
		return toDto(toModel(entity));
	}

	private ServicePriceEntity toEntity(ServicePriceModel model) {
		ServicePriceEntity entity = new ServicePriceEntity();
		entity.setServiceId(model.serviceId());
		entity.setAmount(model.amount());
		entity.setDescription(model.description());
		entity.setValidTo(model.validTo());
		return entity;
	}

	private ServicePriceModel toModel(ServicePriceEntity entity) {
		return new ServicePriceModel(entity.getId(), entity.getServiceId(), entity.getAmount(), entity.getDescription(),
				entity.getValidFrom(), entity.getValidTo());
	}

	private ServicePriceDto toDto(ServicePriceModel model) {
		return new ServicePriceDto(model.id(), model.serviceId(), model.amount(), model.description(),
				model.validFrom(), model.validTo());
	}
}
