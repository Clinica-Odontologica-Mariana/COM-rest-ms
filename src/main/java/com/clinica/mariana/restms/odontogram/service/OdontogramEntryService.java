package com.clinica.mariana.restms.odontogram.service;

import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordEntity;
import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordRepository;
import com.clinica.mariana.restms.odontogram.dto.OdontogramEntryCreateDto;
import com.clinica.mariana.restms.odontogram.dto.OdontogramEntryDto;
import com.clinica.mariana.restms.odontogram.dto.OdontogramEntryUpdateDto;
import com.clinica.mariana.restms.odontogram.entity.OdontogramEntryEntity;
import com.clinica.mariana.restms.odontogram.repository.OdontogramEntryRepository;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OdontogramEntryService {

	private static final String ENTRY_NOT_FOUND = "Odontogram entry not found";
	private static final Set<Integer> INVALID_PERMANENT = Set.of(19, 20, 29, 30, 39, 40);
	private static final Set<Integer> INVALID_DECIDUOUS = Set.of(59, 60, 69, 70, 79, 80);

	private final OdontogramEntryRepository repository;
	private final PatientRepository patientRepository;
	private final MedicalRecordRepository medicalRecordRepository;
	private final ProfessionalRepository professionalRepository;

	public OdontogramEntryService(OdontogramEntryRepository repository, PatientRepository patientRepository,
			MedicalRecordRepository medicalRecordRepository, ProfessionalRepository professionalRepository) {
		this.repository = repository;
		this.patientRepository = patientRepository;
		this.medicalRecordRepository = medicalRecordRepository;
		this.professionalRepository = professionalRepository;
	}

	@Transactional
	public OdontogramEntryDto create(OdontogramEntryCreateDto request) {
		validateTooth(request.toothNumber());
		validatePatientAndRecord(request.patientId(), request.medicalRecordId());
		validateProfessional(request.recordedByProfessionalId());

		OdontogramEntryEntity entity = new OdontogramEntryEntity();
		entity.setPatientId(request.patientId());
		entity.setMedicalRecordId(request.medicalRecordId());
		apply(entity, request.toothNumber(), request.surfaceCode(), request.conditionCode(), request.notes(),
				request.recordedByProfessionalId());
		return toDto(repository.save(entity));
	}

	@Transactional(readOnly = true)
	public OdontogramEntryDto findById(UUID id) {
		return toDto(findEntity(id));
	}

	@Transactional(readOnly = true)
	public List<OdontogramEntryDto> findByPatient(UUID patientId) {
		if (!patientRepository.existsById(patientId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found");
		}
		return repository.findAllByPatientIdOrderByRecordedAtDesc(patientId).stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public List<OdontogramEntryDto> findByMedicalRecord(UUID medicalRecordId) {
		if (!medicalRecordRepository.existsById(medicalRecordId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found");
		}
		return repository.findAllByMedicalRecordIdOrderByRecordedAtDesc(medicalRecordId).stream().map(this::toDto)
				.toList();
	}

	@Transactional
	public OdontogramEntryDto update(UUID id, OdontogramEntryUpdateDto request) {
		validateTooth(request.toothNumber());
		validateProfessional(request.recordedByProfessionalId());
		OdontogramEntryEntity entity = findEntity(id);
		apply(entity, request.toothNumber(), request.surfaceCode(), request.conditionCode(), request.notes(),
				request.recordedByProfessionalId());
		return toDto(repository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		OdontogramEntryEntity entity = findEntity(id);
		repository.delete(entity);
	}

	private OdontogramEntryEntity findEntity(UUID id) {
		return repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ENTRY_NOT_FOUND));
	}

	private void validatePatientAndRecord(UUID patientId, UUID medicalRecordId) {
		if (!patientRepository.existsById(patientId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found");
		}
		MedicalRecordEntity record = medicalRecordRepository.findById(medicalRecordId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found"));
		if (!record.getPatientId().equals(patientId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Medical record does not belong to patient");
		}
	}

	private void validateProfessional(UUID professionalId) {
		if (professionalId != null && !professionalRepository.existsById(professionalId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Professional not found");
		}
	}

	private void validateTooth(Integer toothNumber) {
		boolean permanent = toothNumber >= 11 && toothNumber <= 48 && !INVALID_PERMANENT.contains(toothNumber);
		boolean deciduous = toothNumber >= 51 && toothNumber <= 85 && !INVALID_DECIDUOUS.contains(toothNumber);
		if (!permanent && !deciduous) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid tooth number");
		}
	}

	private void apply(OdontogramEntryEntity entity, Integer toothNumber, String surfaceCode, String conditionCode,
			String notes, UUID professionalId) {
		entity.setToothNumber(toothNumber);
		entity.setSurfaceCode(surfaceCode);
		entity.setConditionCode(conditionCode);
		entity.setNotes(notes);
		entity.setRecordedByProfessionalId(professionalId);
	}

	private OdontogramEntryDto toDto(OdontogramEntryEntity entity) {
		return new OdontogramEntryDto(entity.getId(), entity.getMedicalRecordId(), entity.getPatientId(),
				entity.getToothNumber(), entity.getSurfaceCode(), entity.getConditionCode(), entity.getNotes(),
				entity.getRecordedByProfessionalId(), entity.getRecordedAt());
	}
}
