package com.clinica.mariana.restms.patient.service;

import com.clinica.mariana.restms.medicalrecord.service.MedicalRecordService;
import com.clinica.mariana.restms.patient.dto.PatientCreateDto;
import com.clinica.mariana.restms.patient.dto.PatientDto;
import com.clinica.mariana.restms.patient.dto.PatientUpdateDto;
import com.clinica.mariana.restms.patient.entity.PatientEntity;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clinica.mariana.restms.common.exception.AppException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PatientService {

	private static final String PATIENT_NOT_FOUND = "Patient not found";

	private final PatientRepository patientRepository;
	private final MedicalRecordService medicalRecordService;

	public PatientService(PatientRepository patientRepository, MedicalRecordService medicalRecordService) {
		this.patientRepository = patientRepository;
		this.medicalRecordService = medicalRecordService;
	}

	@Transactional
	public PatientDto create(PatientCreateDto request) {
		if (patientRepository.existsByCpf(request.cpf())) {
			throw new AppException(HttpStatus.CONFLICT, "CPF_ALREADY_EXISTS", "Patient cpf already exists");
		}

		PatientEntity entity = new PatientEntity();
		entity.setAddressId(request.addressId());
		entity.setFullName(request.fullName());
		entity.setCpf(request.cpf());
		entity.setPhone(request.phone());
		entity.setEmail(request.email());
		entity.setBirthDate(request.birthDate());
		entity.setEmergencyContactName(request.emergencyContactName());
		entity.setEmergencyContactPhone(request.emergencyContactPhone());
		entity.setNotes(request.notes());
		entity.setActive(true);
		entity.setInactivatedAt(null);

		PatientEntity saved = patientRepository.save(entity);
		medicalRecordService.createForPatientIfMissing(saved.getId());

		return toDto(saved);
	}

	@Transactional(readOnly = true)
	public Page<PatientDto> findAll(Pageable pageable) {
		return patientRepository.findAllByActiveTrueOrderByFullNameAsc(pageable).map(this::toDto);
	}

	@Transactional(readOnly = true)
	public PatientDto findById(UUID id) {
		PatientEntity entity = patientRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", PATIENT_NOT_FOUND));

		return toDto(entity);
	}

	@Transactional(readOnly = true)
	public PatientDto findByCpf(String cpf) {
		PatientEntity entity = patientRepository.findByCpf(cpf)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", PATIENT_NOT_FOUND));

		return toDto(entity);
	}

	@Transactional
	public PatientDto update(UUID id, PatientUpdateDto request) {
		PatientEntity entity = patientRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", PATIENT_NOT_FOUND));

		if (patientRepository.existsByCpfAndIdNot(request.cpf(), id)) {
			throw new AppException(HttpStatus.CONFLICT, "CPF_ALREADY_EXISTS", "Patient cpf already exists");
		}

		entity.setAddressId(request.addressId());
		entity.setFullName(request.fullName());
		entity.setCpf(request.cpf());
		entity.setPhone(request.phone());
		entity.setEmail(request.email());
		entity.setBirthDate(request.birthDate());
		entity.setEmergencyContactName(request.emergencyContactName());
		entity.setEmergencyContactPhone(request.emergencyContactPhone());
		entity.setNotes(request.notes());

		return toDto(patientRepository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		PatientEntity entity = patientRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", PATIENT_NOT_FOUND));

		if (!entity.isActive()) {
			return;
		}

		entity.setActive(false);
		entity.setInactivatedAt(OffsetDateTime.now());
		patientRepository.save(entity);
	}

	private PatientDto toDto(PatientEntity entity) {
		return new PatientDto(entity.getId(), entity.getAddressId(), entity.getCreatedByUserId(), entity.getFullName(),
				entity.getCpf(), entity.getPhone(), entity.getEmail(), entity.getBirthDate(),
				entity.getEmergencyContactName(), entity.getEmergencyContactPhone(), entity.getNotes(),
				entity.isActive());
	}
}
