package com.clinica.mariana.restms.medicalrecord.test;

import com.clinica.mariana.restms.medicalrecord.controller.MedicalRecordController;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordUpdateDto;
import com.clinica.mariana.restms.medicalrecord.service.MedicalRecordService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MedicalRecordControllerTest {

	private final MedicalRecordService medicalRecordService = mock(MedicalRecordService.class);
	private final MedicalRecordController controller = new MedicalRecordController(medicalRecordService);

	@Test
	void shouldDelegateRequestsToService() {
		UUID id = UUID.randomUUID();
		UUID patientId = UUID.randomUUID();
		MedicalRecordDto dto = dto(id, patientId);
		MedicalRecordCreateDto createDto = new MedicalRecordCreateDto(patientId, "Alergia", null, null, null);
		MedicalRecordUpdateDto updateDto = new MedicalRecordUpdateDto("Nenhuma", null, null, null);

		when(medicalRecordService.create(createDto)).thenReturn(dto);
		when(medicalRecordService.findAll()).thenReturn(List.of(dto));
		when(medicalRecordService.findById(id)).thenReturn(dto);
		when(medicalRecordService.findByPatientId(patientId)).thenReturn(dto);
		when(medicalRecordService.update(id, updateDto)).thenReturn(dto);

		assertThat(controller.create(createDto)).isEqualTo(dto);
		assertThat(controller.findAll()).containsExactly(dto);
		assertThat(controller.findById(id)).isEqualTo(dto);
		assertThat(controller.findByPatientId(patientId)).isEqualTo(dto);
		assertThat(controller.update(id, updateDto)).isEqualTo(dto);

		controller.delete(id);
		verify(medicalRecordService).delete(id);
	}

	private MedicalRecordDto dto(UUID id, UUID patientId) {
		return new MedicalRecordDto(
				id,
				patientId,
				"Paciente",
				"Alergia",
				"Condicao",
				"Medicacao",
				"Observacao",
				LocalDateTime.now(),
				LocalDateTime.now()
		);
	}
}
