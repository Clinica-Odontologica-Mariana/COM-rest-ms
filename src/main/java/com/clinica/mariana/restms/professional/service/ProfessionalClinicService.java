package com.clinica.mariana.restms.professional.service;

import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import com.clinica.mariana.restms.professional.dto.ProfessionalClinicCreateDto;
import com.clinica.mariana.restms.professional.dto.ProfessionalClinicDto;
import com.clinica.mariana.restms.professional.entity.ProfessionalClinicEntity;
import com.clinica.mariana.restms.professional.entity.ProfessionalClinicId;
import com.clinica.mariana.restms.professional.entity.ProfessionalEntity;
import com.clinica.mariana.restms.professional.repository.ProfessionalClinicRepository;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ProfessionalClinicService {

	private static final String CLINIC_NOT_FOUND = "Clinic not found";
	private static final String MEMBERSHIP_NOT_FOUND = "Professional clinic membership not found";
	private static final String PROFESSIONAL_NOT_FOUND = "Professional not found";

	private final ProfessionalRepository professionalRepository;
	private final ClinicRepository clinicRepository;
	private final ProfessionalClinicRepository professionalClinicRepository;

	public ProfessionalClinicService(ProfessionalRepository professionalRepository, ClinicRepository clinicRepository,
			ProfessionalClinicRepository professionalClinicRepository) {
		this.professionalRepository = professionalRepository;
		this.clinicRepository = clinicRepository;
		this.professionalClinicRepository = professionalClinicRepository;
	}

	@Transactional
	public ProfessionalClinicDto create(UUID professionalId, ProfessionalClinicCreateDto request) {
		ProfessionalEntity professional = findProfessional(professionalId);
		validateClinic(request.clinicId());

		ProfessionalClinicId id = new ProfessionalClinicId(professionalId, request.clinicId());
		ProfessionalClinicEntity entity = professionalClinicRepository.findById(id).orElseGet(() -> {
			ProfessionalClinicEntity created = new ProfessionalClinicEntity();
			created.setId(id);
			return created;
		});

		boolean firstMembership = shouldBecomeFirstMembership(professionalId);
		if (request.primaryClinic() || firstMembership) {
			professionalClinicRepository.clearPrimaryClinic(professionalId);
			professional.setClinicId(request.clinicId());
		}

		entity.setActive(true);
		entity.setPrimaryClinic(request.primaryClinic() || firstMembership);

		return toDto(professionalClinicRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public List<ProfessionalClinicDto> findByProfessional(UUID professionalId) {
		validateProfessional(professionalId);
		return professionalClinicRepository
				.findAllByIdProfessionalIdAndActiveTrueOrderByPrimaryClinicDescCreatedAtAsc(professionalId).stream()
				.map(this::toDto).toList();
	}

	@Transactional
	public ProfessionalClinicDto setPrimary(UUID professionalId, UUID clinicId) {
		ProfessionalEntity professional = findProfessional(professionalId);
		validateClinic(clinicId);
		ProfessionalClinicEntity entity = professionalClinicRepository
				.findById(new ProfessionalClinicId(professionalId, clinicId))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MEMBERSHIP_NOT_FOUND));

		if (!entity.isActive()) {
			entity.setActive(true);
		}

		professionalClinicRepository.clearPrimaryClinic(professionalId);
		entity.setPrimaryClinic(true);
		professional.setClinicId(clinicId);

		return toDto(professionalClinicRepository.save(entity));
	}

	@Transactional
	public void deactivate(UUID professionalId, UUID clinicId) {
		ProfessionalEntity professional = findProfessional(professionalId);
		ProfessionalClinicEntity entity = professionalClinicRepository
				.findById(new ProfessionalClinicId(professionalId, clinicId))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MEMBERSHIP_NOT_FOUND));

		entity.setActive(false);
		entity.setPrimaryClinic(false);
		professionalClinicRepository.save(entity);

		professionalClinicRepository
				.findFirstByIdProfessionalIdAndActiveTrueOrderByPrimaryClinicDescCreatedAtAsc(professionalId)
				.ifPresentOrElse(next -> {
					professionalClinicRepository.clearPrimaryClinic(professionalId);
					next.setPrimaryClinic(true);
					professional.setClinicId(next.getId().getClinicId());
					professionalClinicRepository.save(next);
				}, () -> {
					throw new ResponseStatusException(HttpStatus.CONFLICT,
							"Professional must keep at least one active clinic");
				});
	}

	private boolean shouldBecomeFirstMembership(UUID professionalId) {
		return professionalClinicRepository
				.findFirstByIdProfessionalIdAndActiveTrueOrderByPrimaryClinicDescCreatedAtAsc(professionalId).isEmpty();
	}

	private ProfessionalEntity findProfessional(UUID professionalId) {
		return professionalRepository.findById(professionalId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, PROFESSIONAL_NOT_FOUND));
	}

	private void validateProfessional(UUID professionalId) {
		if (!professionalRepository.existsById(professionalId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROFESSIONAL_NOT_FOUND);
		}
	}

	private void validateClinic(UUID clinicId) {
		if (!clinicRepository.existsById(clinicId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, CLINIC_NOT_FOUND);
		}
	}

	private ProfessionalClinicDto toDto(ProfessionalClinicEntity entity) {
		return new ProfessionalClinicDto(entity.getId().getProfessionalId(), entity.getId().getClinicId(),
				entity.isPrimaryClinic(), entity.isActive(), entity.getCreatedAt());
	}
}
