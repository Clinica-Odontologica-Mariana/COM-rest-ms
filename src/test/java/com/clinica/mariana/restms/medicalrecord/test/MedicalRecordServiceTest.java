package com.clinica.mariana.restms.medicalrecord.test;

import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordUpdateDto;
import com.clinica.mariana.restms.medicalrecord.service.MedicalRecordService;
import com.clinica.mariana.restms.patient.dto.PatientCreateDto;
import com.clinica.mariana.restms.patient.dto.PatientDto;
import com.clinica.mariana.restms.patient.service.PatientService;
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
	private PatientService patientService;

	@Test
	void shouldRunMedicalRecordFlowAndValidateErrors() {
		PatientDto patient = patientService.create(new PatientCreateDto(
				"Joao Prontuario",
				"98765432100",
				"61999999999",
				"joao.prontuario@clinic.com",
				LocalDate.of(1985, 3, 20)
		));

		MedicalRecordDto created = medicalRecordService.create(new MedicalRecordCreateDto(
				patient.id(),
				"Penicilina",
				"Hipertensao",
				"Losartana",
				"Paciente em acompanhamento odontologico"
		));

		assertThat(created.id()).isNotNull();
		assertThat(created.patientId()).isEqualTo(patient.id());
		assertThat(created.patientFullName()).isEqualTo("Joao Prontuario");
		assertThat(created.createdAt()).isNotNull();
		assertThat(created.updatedAt()).isNotNull();

		assertThat(medicalRecordService.findAll())
				.extracting(MedicalRecordDto::id)
				.contains(created.id());

		MedicalRecordDto foundById = medicalRecordService.findById(created.id());
		assertThat(foundById.allergies()).isEqualTo("Penicilina");

		MedicalRecordDto foundByPatient = medicalRecordService.findByPatientId(patient.id());
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
				patient.id(),
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
}
