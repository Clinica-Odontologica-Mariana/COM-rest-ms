package com.clinica.mariana.restms.workplace.test;

import com.clinica.mariana.restms.workplace.dto.WorkplaceCreateDto;
import com.clinica.mariana.restms.workplace.dto.WorkplaceDto;
import com.clinica.mariana.restms.workplace.dto.WorkplaceUpdateDto;
import com.clinica.mariana.restms.workplace.service.WorkplaceService;
import com.clinica.mariana.restms.workplace.repository.WorkplaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class WorkplaceControllerTest {

	@Autowired
	private WorkplaceService workplaceService;

	@Autowired
	private WorkplaceRepository workplaceRepository;

	@Autowired
	private Validator validator;

	@BeforeEach
	void cleanDatabase() {
		workplaceRepository.deleteAll();
	}

	@Test
	void shouldRunWorkplaceCrudFlow() {
		UUID clinicId = UUID.randomUUID();

		WorkplaceDto created = workplaceService.create(new WorkplaceCreateDto(
				clinicId,
				"Consultório 01",
				"Primeiro consultório da clínica"
		));

		assertThat(created.id()).isNotNull();
		assertThat(created.active()).isTrue();
		assertThat(created.clinicId()).isEqualTo(clinicId);

		WorkplaceDto found = workplaceService.findById(created.id());
		assertThat(found.id()).isEqualTo(created.id());
		assertThat(found.name()).isEqualTo("Consultório 01");

		WorkplaceDto updated = workplaceService.update(created.id(), new WorkplaceUpdateDto(
				"Consultório 01 - Reformado",
				"Consultório reformado em 2026"
		));

		assertThat(updated.name()).isEqualTo("Consultório 01 - Reformado");
		assertThat(updated.description()).isEqualTo("Consultório reformado em 2026");

		workplaceService.delete(created.id());

		WorkplaceDto afterDelete = workplaceService.findById(created.id());
		assertThat(afterDelete.active()).isFalse();
	}

	@Test
	void shouldFailWhenNameIsDuplicatedForSameClinic() {
		UUID clinicId = UUID.randomUUID();

		workplaceService.create(new WorkplaceCreateDto(clinicId, "Sala A", "desc"));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> workplaceService.create(new WorkplaceCreateDto(clinicId, "Sala A", "outra")));
		assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.CONFLICT);
	}

	@Test
	void shouldNotListInactivatedWorkplace() {
		UUID clinicId = UUID.randomUUID();

		WorkplaceDto created = workplaceService.create(new WorkplaceCreateDto(clinicId, "Sala B", "desc"));
		workplaceService.delete(created.id());

		var list = workplaceService.findAllByClinic(clinicId);
		assertThat(list).isEmpty();
	}

	@Test
	void shouldFailWhenClinicNotFound() {
		UUID clinicId = UUID.randomUUID();

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> workplaceService.create(new WorkplaceCreateDto(clinicId, "Sala C", "desc")));
		assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldValidateBeanPayloads() {
		Set<ConstraintViolation<WorkplaceCreateDto>> violations = validator.validate(new WorkplaceCreateDto(null, "", null));
		assertFalse(violations.isEmpty());

		Set<ConstraintViolation<WorkplaceUpdateDto>> violations2 = validator.validate(new WorkplaceUpdateDto("", null));
		assertFalse(violations2.isEmpty());
	}

	private void insertClinic(UUID clinicId) {
		jdbcTemplate.update(
				"INSERT INTO clinic (id, name, document, phone, timezone, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, TRUE, NOW(), NOW())",
				clinicId, "Clínica Teste", "12345678901234", "999999999", "America/Sao_Paulo");
	}
}
