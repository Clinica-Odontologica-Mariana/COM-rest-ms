package com.clinica.mariana.restms.medicalrecord.service;

import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordUpdateDto;
import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordEntity;
import com.clinica.mariana.restms.medicalrecord.model.MedicalRecordModel;
import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordRepository;
import com.clinica.mariana.restms.patient.entity.PatientEntity;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class MedicalRecordService {

	private final MedicalRecordRepository medicalRecordRepository;
	private final PatientRepository patientRepository;

	public MedicalRecordService(
			MedicalRecordRepository medicalRecordRepository,
			PatientRepository patientRepository
	) {
		this.medicalRecordRepository = medicalRecordRepository;
		this.patientRepository = patientRepository;
	}

	@Transactional
	public MedicalRecordDto create(MedicalRecordCreateDto request) {
		PatientEntity patient = patientRepository.findById(request.patientId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

		if (medicalRecordRepository.existsByPatientId(request.patientId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Medical record already exists for patient");
		}

		MedicalRecordEntity entity = new MedicalRecordEntity();
		entity.setPatient(patient);
		entity.setAllergies(request.allergies());
		entity.setChronicConditions(request.chronicConditions());
		entity.setContinuousMedications(request.continuousMedications());
		entity.setGeneralObservations(request.generalObservations());

		return toDto(toModel(medicalRecordRepository.save(entity)));
	}

	@Transactional(readOnly = true)
	public List<MedicalRecordDto> findAll() {
		return medicalRecordRepository.findAllByOrderByUpdatedAtDesc()
				.stream()
				.map(this::toModel)
				.map(this::toDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public MedicalRecordDto findById(UUID id) {
		MedicalRecordEntity entity = medicalRecordRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found"));

		return toDto(toModel(entity));
	}

	@Transactional(readOnly = true)
	public MedicalRecordDto findByPatientId(UUID patientId) {
		MedicalRecordEntity entity = medicalRecordRepository.findByPatientId(patientId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found"));

		return toDto(toModel(entity));
	}

	@Transactional
	public MedicalRecordDto update(UUID id, MedicalRecordUpdateDto request) {
		MedicalRecordEntity entity = medicalRecordRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found"));

		entity.setAllergies(request.allergies());
		entity.setChronicConditions(request.chronicConditions());
		entity.setContinuousMedications(request.continuousMedications());
		entity.setGeneralObservations(request.generalObservations());

		return toDto(toModel(medicalRecordRepository.save(entity)));
	}

	@Transactional
	public void delete(UUID id) {
		MedicalRecordEntity entity = medicalRecordRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found"));

		medicalRecordRepository.delete(entity);
	}

	private MedicalRecordModel toModel(MedicalRecordEntity entity) {
		PatientEntity patient = entity.getPatient();

		return new MedicalRecordModel(
				entity.getId(),
				patient.getId(),
				patient.getFullName(),
				entity.getAllergies(),
				entity.getChronicConditions(),
				entity.getContinuousMedications(),
				entity.getGeneralObservations(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	private MedicalRecordDto toDto(MedicalRecordModel model) {
		return new MedicalRecordDto(
				model.id(),
				model.patientId(),
				model.patientFullName(),
				model.allergies(),
				model.chronicConditions(),
				model.continuousMedications(),
				model.generalObservations(),
				model.createdAt(),
				model.updatedAt()
		);
	}
}
