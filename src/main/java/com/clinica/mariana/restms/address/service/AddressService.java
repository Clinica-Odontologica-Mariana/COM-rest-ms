package com.clinica.mariana.restms.address.service;

import com.clinica.mariana.restms.address.dto.AddressCreateDto;
import com.clinica.mariana.restms.address.dto.AddressDto;
import com.clinica.mariana.restms.address.dto.AddressUpdateDto;
import com.clinica.mariana.restms.address.entity.AddressEntity;
import com.clinica.mariana.restms.address.model.AddressModel;
import com.clinica.mariana.restms.address.repository.AddressRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clinica.mariana.restms.common.exception.AppException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Service
public class AddressService {

	private final AddressRepository addressRepository;

	public AddressService(AddressRepository addressRepository) {
		this.addressRepository = addressRepository;
	}

	@Transactional
	public AddressDto create(AddressCreateDto request) {
		AddressModel model = AddressModel.create(request.street(), request.number(), request.complement(),
				request.neighborhood(), request.city(), request.state(), request.zipCode());

		return toDto(toModel(addressRepository.save(toEntity(model))));
	}

	@Transactional(readOnly = true)
	public Page<AddressDto> findAll(Pageable pageable) {
		return addressRepository.findAllByOrderByCityAscStreetAsc(pageable).map(this::toModel).map(this::toDto);
	}

	@Transactional(readOnly = true)
	public AddressDto findById(UUID id) {
		AddressEntity entity = addressRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Address not found"));

		return toDto(toModel(entity));
	}

	@Transactional
	public AddressDto update(UUID id, AddressUpdateDto request) {
		AddressEntity entity = addressRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Address not found"));

		AddressModel model = new AddressModel(id, request.street(), request.number(), request.complement(),
				request.neighborhood(), request.city(), request.state(), request.zipCode());
		apply(entity, model);

		return toDto(toModel(addressRepository.save(entity)));
	}

	@Transactional
	public void delete(UUID id) {
		if (!addressRepository.existsById(id)) {
			throw new AppException(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Address not found");
		}

		try {
			addressRepository.deleteById(id);
			addressRepository.flush();
		} catch (DataIntegrityViolationException ex) {
			throw new AppException(HttpStatus.CONFLICT, "ADDRESS_IN_USE", "Address is in use");
		}
	}

	private AddressEntity toEntity(AddressModel model) {
		AddressEntity entity = new AddressEntity();
		apply(entity, model);
		return entity;
	}

	private void apply(AddressEntity entity, AddressModel model) {
		entity.setStreet(model.street());
		entity.setNumber(model.number());
		entity.setComplement(model.complement());
		entity.setNeighborhood(model.neighborhood());
		entity.setCity(model.city());
		entity.setState(model.state());
		entity.setZipCode(model.zipCode());
	}

	private AddressModel toModel(AddressEntity entity) {
		return new AddressModel(entity.getId(), entity.getStreet(), entity.getNumber(), entity.getComplement(),
				entity.getNeighborhood(), entity.getCity(), entity.getState(), entity.getZipCode());
	}

	private AddressDto toDto(AddressModel model) {
		return new AddressDto(model.id(), model.street(), model.number(), model.complement(), model.neighborhood(),
				model.city(), model.state(), model.zipCode());
	}
}
