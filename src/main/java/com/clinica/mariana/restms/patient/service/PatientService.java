package com.clinica.mariana.restms.patient.service;

import com.clinica.mariana.restms.patient.dto.PatientCreateDto;
import com.clinica.mariana.restms.patient.dto.PatientDto;
import com.clinica.mariana.restms.patient.dto.PatientUpdateDto;
import com.clinica.mariana.restms.patient.entity.PatientEntity;
import com.clinica.mariana.restms.patient.model.PatientModel;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

	private final PatientRepository patientRepository;

	public PatientService(PatientRepository patientRepository) {
		this.patientRepository = patientRepository;
	}

	@Transactional
	public PatientDto create(PatientCreateDto request) {
		if (patientRepository.existsByCpf(request.cpf())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient cpf already exists");
		}

		PatientEntity entity = new PatientEntity();
		entity.setFullName(request.fullName());
		entity.setCpf(request.cpf());
		entity.setPhone(request.phone());
		entity.setEmail(request.email());
		entity.setBirthDate(request.birthDate());
		entity.setActive(true);
		entity.setInactivatedAt(null);

		return toDto(toModel(patientRepository.save(entity)));
	}

	@Transactional(readOnly = true)
	public List<PatientDto> findAll() {
		return patientRepository.findAllByActiveTrueOrderByFullNameAsc()
				.stream()
				.map(this::toModel)
				.map(this::toDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public PatientDto findById(UUID id) {
		PatientEntity entity = patientRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

		return toDto(toModel(entity));
	}

	@Transactional(readOnly = true)
	public PatientDto findByCpf(String cpf) {
		PatientEntity entity = patientRepository.findByCpf(cpf)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

		return toDto(toModel(entity));
	}

	@Transactional
	public PatientDto update(UUID id, PatientUpdateDto request) {
		PatientEntity entity = patientRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

		if (patientRepository.existsByCpfAndIdNot(request.cpf(), id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient cpf already exists");
		}

		entity.setFullName(request.fullName());
		entity.setCpf(request.cpf());
		entity.setPhone(request.phone());
		entity.setEmail(request.email());
		entity.setBirthDate(request.birthDate());

		return toDto(toModel(patientRepository.save(entity)));
	}

	@Transactional
	public void delete(UUID id) {
		PatientEntity entity = patientRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

		if (!entity.isActive()) {
			return;
		}

		entity.setActive(false);
		entity.setInactivatedAt(LocalDateTime.now());
		patientRepository.save(entity);
	}

	@Transactional(readOnly = true)
	public PatientDto example() {
		PatientModel model = new PatientModel(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"Paciente Exemplo",
				"12345678901",
				"11999999999",
				"paciente.exemplo@clinic.com",
				LocalDate.of(1995, 5, 15),
				true
		);

		return toDto(model);
	}

	private PatientModel toModel(PatientEntity entity) {
		return new PatientModel(
				entity.getId(),
				entity.getFullName(),
				entity.getCpf(),
				entity.getPhone(),
				entity.getEmail(),
				entity.getBirthDate(),
				entity.isActive()
		);
	}

	private PatientDto toDto(PatientModel model) {
		return new PatientDto(
				model.id(),
				model.fullName(),
				model.cpf(),
				model.phone(),
				model.email(),
				model.birthDate(),
				model.active()
		);
	}
}
