package com.clinica.mariana.restms.medicalrecord.test;

import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordUpdateDto;
import com.clinica.mariana.restms.medicalrecord.service.MedicalRecordService;
import com.clinica.mariana.restms.patient.entity.PatientEntity;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class MedicalRecordServiceTest {

	@Autowired
	private MedicalRecordService medicalRecordService;

	@Autowired
	private PatientRepository patientRepository;

	@Test
	void shouldRunMedicalRecordFlowAndValidateErrors() {
		PatientEntity patient = patientRepository.save(patient(
				"Joao Prontuario",
				"98765432100"
		));

		MedicalRecordDto created = medicalRecordService.create(new MedicalRecordCreateDto(
				patient.getId(),
				"Penicilina",
				"Hipertensao",
				"Losartana",
				"Paciente em acompanhamento odontologico"
		));

		assertThat(created.id()).isNotNull();
		assertThat(created.patientId()).isEqualTo(patient.getId());

		assertThat(medicalRecordService.findAll())
				.extracting(MedicalRecordDto::id)
				.contains(created.id());

		MedicalRecordDto foundById = medicalRecordService.findById(created.id());
		assertThat(foundById.allergies()).isEqualTo("Penicilina");

		MedicalRecordDto foundByPatient = medicalRecordService.findByPatientId(patient.getId());
		assertThat(foundByPatient.id()).isEqualTo(created.id());

		MedicalRecordDto updated = medicalRecordService.update(created.id(), new MedicalRecordUpdateDto(
				"Nenhuma alergia conhecida",
				"Hipertensao controlada",
				"Losartana 50mg",
				"Retorno em seis meses"
		));

		assertThat(updated.allergies()).isEqualTo("Nenhuma alergia conhecida");
		assertThat(updated.generalObservations()).isEqualTo("Retorno em seis meses");

		assertStatus(HttpStatus.CONFLICT, () -> medicalRecordService.create(new MedicalRecordCreateDto(
				patient.getId(),
				null,
				null,
				null,
				null
		)));

		UUID unknownId = UUID.randomUUID();
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.create(new MedicalRecordCreateDto(
				unknownId,
				null,
				null,
				null,
				null
		)));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.findById(unknownId));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.findByPatientId(unknownId));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.update(unknownId, new MedicalRecordUpdateDto(
				null,
				null,
				null,
				null
		)));

		medicalRecordService.delete(created.id());
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.findById(created.id()));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.delete(created.id()));
	}

	private void assertStatus(HttpStatus status, Runnable action) {
		assertThatThrownBy(action::run)
				.isInstanceOf(ResponseStatusException.class)
				.extracting("statusCode")
				.isEqualTo(status);
	}

	private PatientEntity patient(String fullName, String cpf) {
		PatientEntity entity = new PatientEntity();
		entity.setFullName(fullName);
		entity.setCpf(cpf);
		entity.setPhone("61999999999");
		entity.setEmail(cpf + "@clinic.com");
		entity.setBirthDate(LocalDate.of(1985, 3, 20));
		entity.setActive(true);
		return entity;
	}
}
