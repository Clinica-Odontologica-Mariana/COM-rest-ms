package com.clinica.mariana.restms.professional.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clinica.mariana.restms.common.exception.AppException;

import com.clinica.mariana.restms.professional.dto.ProfessionalCreateDto;
import com.clinica.mariana.restms.professional.dto.ProfessionalDto;
import com.clinica.mariana.restms.professional.dto.ProfessionalUpdateDto;
import com.clinica.mariana.restms.professional.entity.ProfessionalEntity;
import com.clinica.mariana.restms.professional.repository.ProfessionalReferenceRepository;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;

@Service
public class ProfessionalService {

	private final ProfessionalRepository professionalRepository;
	private final ProfessionalReferenceRepository referenceRepository;

	public ProfessionalService(ProfessionalRepository professionalRepository,
			ProfessionalReferenceRepository referenceRepository) {
		this.professionalRepository = professionalRepository;
		this.referenceRepository = referenceRepository;
	}

	@Transactional
	public ProfessionalDto create(ProfessionalCreateDto request) {
		validateReferences(request.userId(), request.clinicId(), request.specialtyId());
		validateUniqueUser(request.userId(), null);
		validateUniqueLicenseInClinic(request.clinicId(), request.licenseNumber(), null);

		ProfessionalEntity entity = new ProfessionalEntity();
		entity.setUserId(request.userId());
		entity.setClinicId(request.clinicId());
		entity.setSpecialtyId(request.specialtyId());
		entity.setLicenseNumber(request.licenseNumber());
		entity.setActive(true);
		entity.setInactivatedAt(null);

		return toDto(professionalRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public Page<ProfessionalDto> findAll(Pageable pageable) {
		return professionalRepository.findAllByActiveTrueOrderByLicenseNumberAsc(pageable).map(this::toDto);
	}

	@Transactional(readOnly = true)
	public ProfessionalDto findById(UUID id) {
		ProfessionalEntity entity = professionalRepository.findById(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "PROFESSIONAL_NOT_FOUND", "Professional not found"));

		return toDto(entity);
	}

	@Transactional
	public ProfessionalDto update(UUID id, ProfessionalUpdateDto request) {
		ProfessionalEntity entity = professionalRepository.findById(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "PROFESSIONAL_NOT_FOUND", "Professional not found"));

		validateReferences(request.userId(), request.clinicId(), request.specialtyId());
		validateUniqueUser(request.userId(), id);
		validateUniqueLicenseInClinic(request.clinicId(), request.licenseNumber(), id);

		entity.setUserId(request.userId());
		entity.setClinicId(request.clinicId());
		entity.setSpecialtyId(request.specialtyId());
		entity.setLicenseNumber(request.licenseNumber());

		return toDto(professionalRepository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		ProfessionalEntity entity = professionalRepository.findById(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "PROFESSIONAL_NOT_FOUND", "Professional not found"));

		if (!entity.isActive()) {
			return;
		}

		entity.setActive(false);
		entity.setInactivatedAt(OffsetDateTime.now());
		professionalRepository.save(entity);
	}

	private void validateReferences(UUID userId, UUID clinicId, UUID specialtyId) {
		if (!referenceRepository.userExists(userId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found");
		}
		if (!referenceRepository.clinicExists(clinicId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "CLINIC_NOT_FOUND", "Clinic not found");
		}
		if (!referenceRepository.specialtyExists(specialtyId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "SPECIALTY_NOT_FOUND", "Specialty not found");
		}
	}

	private void validateUniqueUser(UUID userId, UUID professionalIdToIgnore) {
		boolean alreadyExists = professionalIdToIgnore == null
				? professionalRepository.existsByUserId(userId)
				: professionalRepository.existsByUserIdAndIdNot(userId, professionalIdToIgnore);

		if (alreadyExists) {
			throw new AppException(HttpStatus.CONFLICT, "PROFESSIONAL_ALREADY_EXISTS",
					"User already has a professional profile");
		}
	}

	private void validateUniqueLicenseInClinic(UUID clinicId, String licenseNumber, UUID professionalIdToIgnore) {
		boolean alreadyExists = professionalIdToIgnore == null
				? professionalRepository.existsByClinicIdAndLicenseNumber(clinicId, licenseNumber)
				: professionalRepository.existsByClinicIdAndLicenseNumberAndIdNot(clinicId, licenseNumber,
						professionalIdToIgnore);

		if (alreadyExists) {
			throw new AppException(HttpStatus.CONFLICT, "LICENSE_ALREADY_EXISTS",
					"Professional license already exists in this clinic");
		}
	}

	private ProfessionalDto toDto(ProfessionalEntity entity) {
		return new ProfessionalDto(entity.getId(), entity.getUserId(), entity.getClinicId(), entity.getSpecialtyId(),
				entity.getLicenseNumber(), entity.isActive());
	}
}
