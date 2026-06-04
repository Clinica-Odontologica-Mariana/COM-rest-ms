package com.clinica.mariana.restms.patient.service;

import com.clinica.mariana.restms.common.exception.AppException;

import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import com.clinica.mariana.restms.patient.dto.PatientClinicCreateDto;
import com.clinica.mariana.restms.patient.dto.PatientClinicDto;
import com.clinica.mariana.restms.patient.entity.PatientClinicEntity;
import com.clinica.mariana.restms.patient.entity.PatientClinicId;
import com.clinica.mariana.restms.patient.repository.PatientClinicRepository;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PatientClinicService {

	private static final String CLINIC_NOT_FOUND = "Clinic not found";
	private static final String MEMBERSHIP_NOT_FOUND = "Patient clinic membership not found";
	private static final String PATIENT_NOT_FOUND = "Patient not found";

	private final PatientRepository patientRepository;
	private final ClinicRepository clinicRepository;
	private final PatientClinicRepository patientClinicRepository;

	public PatientClinicService(PatientRepository patientRepository, ClinicRepository clinicRepository,
			PatientClinicRepository patientClinicRepository) {
		this.patientRepository = patientRepository;
		this.clinicRepository = clinicRepository;
		this.patientClinicRepository = patientClinicRepository;
	}

	@Transactional
	public PatientClinicDto create(UUID patientId, PatientClinicCreateDto request) {
		validatePatientAndClinic(patientId, request.clinicId());

		PatientClinicId id = new PatientClinicId(patientId, request.clinicId());
		PatientClinicEntity entity = patientClinicRepository.findById(id).orElseGet(() -> {
			PatientClinicEntity created = new PatientClinicEntity();
			created.setId(id);
			return created;
		});

		if (request.primaryClinic()) {
			patientClinicRepository.clearPrimaryClinic(patientId);
		}

		entity.setActive(true);
		entity.setPrimaryClinic(request.primaryClinic() || shouldBecomeFirstMembership(patientId));

		return toDto(patientClinicRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public List<PatientClinicDto> findByPatient(UUID patientId) {
		validatePatient(patientId);
		return patientClinicRepository.findAllByIdPatientIdAndActiveTrueOrderByPrimaryClinicDescCreatedAtAsc(patientId)
				.stream().map(this::toDto).toList();
	}

	@Transactional
	public PatientClinicDto setPrimary(UUID patientId, UUID clinicId) {
		validatePatientAndClinic(patientId, clinicId);
		PatientClinicEntity entity = patientClinicRepository.findById(new PatientClinicId(patientId, clinicId))
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "MEMBERSHIP_NOT_FOUND", MEMBERSHIP_NOT_FOUND));

		if (!entity.isActive()) {
			entity.setActive(true);
		}

		patientClinicRepository.clearPrimaryClinic(patientId);
		entity.setPrimaryClinic(true);

		return toDto(patientClinicRepository.save(entity));
	}

	@Transactional
	public void deactivate(UUID patientId, UUID clinicId) {
		PatientClinicEntity entity = patientClinicRepository.findById(new PatientClinicId(patientId, clinicId))
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "MEMBERSHIP_NOT_FOUND", MEMBERSHIP_NOT_FOUND));

		entity.setActive(false);
		entity.setPrimaryClinic(false);
		patientClinicRepository.save(entity);

		patientClinicRepository.findFirstByIdPatientIdAndActiveTrueOrderByPrimaryClinicDescCreatedAtAsc(patientId)
				.ifPresent(next -> {
					if (!next.isPrimaryClinic()) {
						patientClinicRepository.clearPrimaryClinic(patientId);
						next.setPrimaryClinic(true);
						patientClinicRepository.save(next);
					}
				});
	}

	private boolean shouldBecomeFirstMembership(UUID patientId) {
		return patientClinicRepository
				.findFirstByIdPatientIdAndActiveTrueOrderByPrimaryClinicDescCreatedAtAsc(patientId).isEmpty();
	}

	private void validatePatientAndClinic(UUID patientId, UUID clinicId) {
		validatePatient(patientId);
		if (!clinicRepository.existsById(clinicId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "CLINIC_NOT_FOUND", CLINIC_NOT_FOUND);
		}
	}

	private void validatePatient(UUID patientId) {
		if (!patientRepository.existsById(patientId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", PATIENT_NOT_FOUND);
		}
	}

	private PatientClinicDto toDto(PatientClinicEntity entity) {
		return new PatientClinicDto(entity.getId().getPatientId(), entity.getId().getClinicId(),
				entity.isPrimaryClinic(), entity.isActive(), entity.getCreatedAt());
	}
}
