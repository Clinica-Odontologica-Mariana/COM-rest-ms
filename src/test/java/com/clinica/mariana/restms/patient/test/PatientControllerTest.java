package com.clinica.mariana.restms.patient.test;

import com.clinica.mariana.restms.patient.dto.PatientCreateDto;
import com.clinica.mariana.restms.patient.dto.PatientDto;
import com.clinica.mariana.restms.patient.dto.PatientUpdateDto;
import com.clinica.mariana.restms.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PatientControllerTest {

	@Autowired
	private PatientService patientService;

	@Test
	void shouldRunPatientCrudFlow() throws Exception {
		PatientDto created = patientService.create(new PatientCreateDto(
				"Maria Silva",
				"12345678901",
				"11999999999",
				"maria.silva@clinic.com",
				LocalDate.of(1990, 1, 10)
		));

		assertThat(created.id()).isNotNull();
		assertThat(created.active()).isTrue();

		PatientDto found = patientService.findById(created.id());
		assertThat(found.id()).isEqualTo(created.id());

		PatientDto updated = patientService.update(created.id(), new PatientUpdateDto(
				"Maria Silva Atualizada",
				"12345678901",
				"11888888888",
				"maria.atualizada@clinic.com",
				LocalDate.of(1990, 1, 10)
		));

		assertThat(updated.fullName()).isEqualTo("Maria Silva Atualizada");
		assertThat(updated.phone()).isEqualTo("11888888888");

		patientService.delete(created.id());

		PatientDto afterDelete = patientService.findById(created.id());
		assertThat(afterDelete.active()).isFalse();
	}
}
