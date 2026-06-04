package com.clinica.mariana.restms.clinic.service;

import com.clinica.mariana.restms.address.repository.AddressRepository;
import com.clinica.mariana.restms.clinic.dto.ClinicCreateDto;
import com.clinica.mariana.restms.clinic.dto.ClinicDto;
import com.clinica.mariana.restms.clinic.dto.ClinicUpdateDto;
import com.clinica.mariana.restms.clinic.entity.ClinicEntity;
import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clinica.mariana.restms.common.exception.AppException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ClinicService {

	private static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";
	private static final String CLINIC_NOT_FOUND = "Clinic not found";

	private final ClinicRepository clinicRepository;
	private final AddressRepository addressRepository;

	public ClinicService(ClinicRepository clinicRepository, AddressRepository addressRepository) {
		this.clinicRepository = clinicRepository;
		this.addressRepository = addressRepository;
	}

	@Transactional
	public ClinicDto create(ClinicCreateDto request) {
		validateAddress(request.addressId());
		if (clinicRepository.existsByDocument(request.document())) {
			throw new AppException(HttpStatus.CONFLICT, "CLINIC_DOCUMENT_ALREADY_EXISTS", "Clinic document already exists");
		}
		ClinicEntity entity = new ClinicEntity();
		apply(entity, request.addressId(), request.name(), request.document(), request.phone(), request.email(),
				request.timezone(), request.description());
		entity.setActive(true);
		entity.setInactivatedAt(null);
		return toDto(clinicRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public Page<ClinicDto> findAll(Pageable pageable) {
		return clinicRepository.findAllByActiveTrueOrderByNameAsc(pageable).map(this::toDto);
	}

	@Transactional(readOnly = true)
	public ClinicDto findById(UUID id) {
		return toDto(findEntity(id));
	}

	@Transactional(readOnly = true)
	public ClinicDto findByDocument(String document) {
		return clinicRepository.findByDocument(document).map(this::toDto)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "CLINIC_NOT_FOUND", CLINIC_NOT_FOUND));
	}

	@Transactional
	public ClinicDto update(UUID id, ClinicUpdateDto request) {
		ClinicEntity entity = findEntity(id);
		validateAddress(request.addressId());
		if (clinicRepository.existsByDocumentAndIdNot(request.document(), id)) {
			throw new AppException(HttpStatus.CONFLICT, "CLINIC_DOCUMENT_ALREADY_EXISTS", "Clinic document already exists");
		}
		apply(entity, request.addressId(), request.name(), request.document(), request.phone(), request.email(),
				request.timezone(), request.description());
		return toDto(clinicRepository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		ClinicEntity entity = findEntity(id);
		if (!entity.isActive()) {
			return;
		}
		entity.setActive(false);
		entity.setInactivatedAt(OffsetDateTime.now());
		clinicRepository.save(entity);
	}

	private ClinicEntity findEntity(UUID id) {
		return clinicRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "CLINIC_NOT_FOUND", CLINIC_NOT_FOUND));
	}

	private void validateAddress(UUID addressId) {
		if (addressId != null && !addressRepository.existsById(addressId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Address not found");
		}
	}

	private void apply(ClinicEntity entity, UUID addressId, String name, String document, String phone, String email,
			String timezone, String description) {
		entity.setAddressId(addressId);
		entity.setName(name);
		entity.setDocument(document);
		entity.setPhone(phone);
		entity.setEmail(email);
		entity.setTimezone(timezone == null || timezone.isBlank() ? DEFAULT_TIMEZONE : timezone);
		entity.setDescription(description);
	}

	private ClinicDto toDto(ClinicEntity entity) {
		return new ClinicDto(entity.getId(), entity.getAddressId(), entity.getName(), entity.getDocument(),
				entity.getPhone(), entity.getEmail(), entity.getTimezone(), entity.getDescription(), entity.isActive(),
				entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
