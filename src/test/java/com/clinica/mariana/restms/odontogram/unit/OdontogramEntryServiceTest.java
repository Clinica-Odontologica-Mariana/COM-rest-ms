package com.clinica.mariana.restms.odontogram.unit;

import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordRepository;
import com.clinica.mariana.restms.odontogram.dto.OdontogramEntryCreateDto;
import com.clinica.mariana.restms.odontogram.repository.OdontogramEntryRepository;
import com.clinica.mariana.restms.odontogram.service.OdontogramEntryService;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class OdontogramEntryServiceTest {

	@Mock
	private OdontogramEntryRepository repository;

	@Mock
	private PatientRepository patientRepository;

	@Mock
	private MedicalRecordRepository medicalRecordRepository;

	@Mock
	private ProfessionalRepository professionalRepository;

	@InjectMocks
	private OdontogramEntryService service;

	@Test
	void shouldRejectInvalidToothNumber() {
		OdontogramEntryCreateDto request = new OdontogramEntryCreateDto(UUID.randomUUID(), UUID.randomUUID(), 19, null,
				"CARIES", null, null);

		assertThatThrownBy(() -> service.create(request)).isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Invalid tooth number");
	}
}
