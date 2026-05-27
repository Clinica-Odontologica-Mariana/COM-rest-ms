package com.clinica.mariana.restms.patient.unit;

import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import com.clinica.mariana.restms.patient.dto.PatientClinicCreateDto;
import com.clinica.mariana.restms.patient.entity.PatientClinicEntity;
import com.clinica.mariana.restms.patient.repository.PatientClinicRepository;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import com.clinica.mariana.restms.patient.service.PatientClinicService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientClinicServiceTest {

	@Mock
	private PatientRepository patientRepository;

	@Mock
	private ClinicRepository clinicRepository;

	@Mock
	private PatientClinicRepository patientClinicRepository;

	@InjectMocks
	private PatientClinicService service;

	@Test
	void shouldCreatePrimaryMembershipWhenRequested() {
		UUID patientId = UUID.randomUUID();
		UUID clinicId = UUID.randomUUID();

		when(patientRepository.existsById(patientId)).thenReturn(true);
		when(clinicRepository.existsById(clinicId)).thenReturn(true);
		when(patientClinicRepository.findById(any())).thenReturn(Optional.empty());
		when(patientClinicRepository.save(any(PatientClinicEntity.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		var result = service.create(patientId, new PatientClinicCreateDto(clinicId, true));

		assertThat(result.patientId()).isEqualTo(patientId);
		assertThat(result.clinicId()).isEqualTo(clinicId);
		assertThat(result.primaryClinic()).isTrue();
		assertThat(result.active()).isTrue();
		verify(patientClinicRepository).clearPrimaryClinic(patientId);
	}

	@Test
	void shouldRejectUnknownClinic() {
		UUID patientId = UUID.randomUUID();
		UUID clinicId = UUID.randomUUID();

		when(patientRepository.existsById(patientId)).thenReturn(true);
		when(clinicRepository.existsById(clinicId)).thenReturn(false);

		assertThatThrownBy(() -> service.create(patientId, new PatientClinicCreateDto(clinicId, false)))
				.isInstanceOf(ResponseStatusException.class).hasMessageContaining("Clinic not found");
	}
}
