package com.clinica.mariana.restms.professional.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.clinica.mariana.restms.professional.dto.ProfessionalCreateDto;
import com.clinica.mariana.restms.professional.dto.ProfessionalDto;
import com.clinica.mariana.restms.professional.dto.ProfessionalUpdateDto;
import com.clinica.mariana.restms.professional.entity.ProfessionalEntity;
import com.clinica.mariana.restms.professional.model.ProfessionalModel;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;

@Service
public class ProfessionalService {

	private final ProfessionalRepository professionalRepository;

	public ProfessionalService(ProfessionalRepository professionalRepository) {
		this.professionalRepository = professionalRepository;
	}

	@Transactional
	public ProfessionalDto create(ProfessionalCreateDto request) {
		validateUniqueLicenseInClinic(request.clinicId(), request.licenseNumber(), null);

		ProfessionalEntity entity = new ProfessionalEntity();
		entity.setUserId(request.userId());
		entity.setClinicId(request.clinicId());
		entity.setSpecialtyId(request.specialtyId());
		entity.setLicenseNumber(request.licenseNumber());
		entity.setActive(true);
		entity.setInactivatedAt(null);

		return toDto(toModel(professionalRepository.save(entity)));
	}

	@Transactional(readOnly = true)
	public List<ProfessionalDto> findAll() {
		return professionalRepository.findAllByActiveTrueOrderByLicenseNumberAsc()
				.stream()
				.map(this::toModel)
				.map(this::toDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public ProfessionalDto findById(UUID id) {
		ProfessionalEntity entity = professionalRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professional not found"));

		return toDto(toModel(entity));
	}

	@Transactional
	public ProfessionalDto update(UUID id, ProfessionalUpdateDto request) {
		ProfessionalEntity entity = professionalRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professional not found"));

		validateUniqueLicenseInClinic(request.clinicId(), request.licenseNumber(), id);

		entity.setUserId(request.userId());
		entity.setClinicId(request.clinicId());
		entity.setSpecialtyId(request.specialtyId());
		entity.setLicenseNumber(request.licenseNumber());

		return toDto(toModel(professionalRepository.save(entity)));
	}

	@Transactional
	public void delete(UUID id) {
		ProfessionalEntity entity = professionalRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professional not found"));

		if (!entity.isActive()) {
			return;
		}

		entity.setActive(false);
		entity.setInactivatedAt(OffsetDateTime.now());
		professionalRepository.save(entity);
	}

	private void validateUniqueLicenseInClinic(UUID clinicId, String licenseNumber, UUID professionalIdToIgnore) {
		boolean alreadyExists = professionalIdToIgnore == null
				? professionalRepository.existsByClinicIdAndLicenseNumber(clinicId, licenseNumber)
				: professionalRepository.existsByClinicIdAndLicenseNumberAndIdNot(clinicId, licenseNumber,
						professionalIdToIgnore);

		if (alreadyExists) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Professional license already exists in this clinic");
		}
	}

	private ProfessionalModel toModel(ProfessionalEntity entity) {
		return new ProfessionalModel(
				entity.getId(),
				entity.getUserId(),
				entity.getClinicId(),
				entity.getSpecialtyId(),
				entity.getLicenseNumber(),
				entity.isActive());
	}

	private ProfessionalDto toDto(ProfessionalModel model) {
		return new ProfessionalDto(
				model.id(),
				model.userId(),
				model.clinicId(),
				model.specialtyId(),
				model.licenseNumber(),
				model.active());
	}
}
