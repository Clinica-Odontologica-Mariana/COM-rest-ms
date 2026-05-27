package com.clinica.mariana.restms.professional.unit;

import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import com.clinica.mariana.restms.professional.dto.ProfessionalClinicCreateDto;
import com.clinica.mariana.restms.professional.entity.ProfessionalClinicEntity;
import com.clinica.mariana.restms.professional.entity.ProfessionalEntity;
import com.clinica.mariana.restms.professional.repository.ProfessionalClinicRepository;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;
import com.clinica.mariana.restms.professional.service.ProfessionalClinicService;
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
class ProfessionalClinicServiceTest {

	@Mock
	private ProfessionalRepository professionalRepository;

	@Mock
	private ClinicRepository clinicRepository;

	@Mock
	private ProfessionalClinicRepository professionalClinicRepository;

	@InjectMocks
	private ProfessionalClinicService service;

	@Test
	void shouldCreatePrimaryMembershipAndUpdateProfessionalPrimaryClinic() {
		UUID professionalId = UUID.randomUUID();
		UUID clinicId = UUID.randomUUID();
		ProfessionalEntity professional = new ProfessionalEntity();
		professional.setId(professionalId);

		when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
		when(clinicRepository.existsById(clinicId)).thenReturn(true);
		when(professionalClinicRepository.findById(any())).thenReturn(Optional.empty());
		when(professionalClinicRepository.save(any(ProfessionalClinicEntity.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		var result = service.create(professionalId, new ProfessionalClinicCreateDto(clinicId, true));

		assertThat(result.professionalId()).isEqualTo(professionalId);
		assertThat(result.clinicId()).isEqualTo(clinicId);
		assertThat(result.primaryClinic()).isTrue();
		assertThat(professional.getClinicId()).isEqualTo(clinicId);
		verify(professionalClinicRepository).clearPrimaryClinic(professionalId);
	}

	@Test
	void shouldRejectRemovingLastActiveClinic() {
		UUID professionalId = UUID.randomUUID();
		UUID clinicId = UUID.randomUUID();
		ProfessionalEntity professional = new ProfessionalEntity();
		professional.setId(professionalId);
		ProfessionalClinicEntity membership = new ProfessionalClinicEntity();
		membership.setId(
				new com.clinica.mariana.restms.professional.entity.ProfessionalClinicId(professionalId, clinicId));
		membership.setActive(true);
		membership.setPrimaryClinic(true);

		when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
		when(professionalClinicRepository.findById(any())).thenReturn(Optional.of(membership));
		when(professionalClinicRepository
				.findFirstByIdProfessionalIdAndActiveTrueOrderByPrimaryClinicDescCreatedAtAsc(professionalId))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.deactivate(professionalId, clinicId))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("Professional must keep at least one active clinic");
	}
}
