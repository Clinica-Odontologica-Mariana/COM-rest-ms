package com.clinica.mariana.restms.workplace.test;

import com.clinica.mariana.restms.workplace.dto.WorkplaceCreateDto;
import com.clinica.mariana.restms.workplace.dto.WorkplaceDto;
import com.clinica.mariana.restms.workplace.dto.WorkplaceUpdateDto;
import com.clinica.mariana.restms.workplace.service.WorkplaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WorkplaceControllerTest {

	@Autowired
	private WorkplaceService workplaceService;

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
}
