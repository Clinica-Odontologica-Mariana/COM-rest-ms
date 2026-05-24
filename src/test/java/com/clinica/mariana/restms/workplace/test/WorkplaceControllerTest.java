package com.clinica.mariana.restms.workplace.test;

import com.clinica.mariana.restms.workplace.dto.WorkplaceCreateDto;
import com.clinica.mariana.restms.workplace.dto.WorkplaceDto;
import com.clinica.mariana.restms.workplace.dto.WorkplaceUpdateDto;
import com.clinica.mariana.restms.workplace.service.WorkplaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class WorkplaceControllerTest {

	@Autowired
	private WorkplaceService workplaceService;

	@Test
	void shouldRunWorkplaceCrudFlow() {
		UUID clinicId = UUID.randomUUID();

		WorkplaceDto created = workplaceService
				.create(new WorkplaceCreateDto(clinicId, "Consultório 01", "Primeiro consultório da clínica"));

		assertThat(created.id()).isNotNull();
		assertThat(created.active()).isTrue();
		assertThat(created.clinicId()).isEqualTo(clinicId);

		WorkplaceDto found = workplaceService.findById(created.id());
		assertThat(found.id()).isEqualTo(created.id());
		assertThat(found.name()).isEqualTo("Consultório 01");

		WorkplaceDto updated = workplaceService.update(created.id(),
				new WorkplaceUpdateDto("Consultório 01 - Reformado", "Consultório reformado em 2026"));

		assertThat(updated.name()).isEqualTo("Consultório 01 - Reformado");
		assertThat(updated.description()).isEqualTo("Consultório reformado em 2026");

		workplaceService.delete(created.id());

		WorkplaceDto afterDelete = workplaceService.findById(created.id());
		assertThat(afterDelete.active()).isFalse();
	}

	@Test
	void shouldFailWhenNameIsDuplicatedForSameClinic() {
		UUID clinicId = UUID.randomUUID();
		String duplicateName = "Consultório Duplicado";

		workplaceService.create(new WorkplaceCreateDto(clinicId, duplicateName, "Primeiro"));

		assertThatThrownBy(() -> workplaceService.create(new WorkplaceCreateDto(clinicId, duplicateName, "Segundo")))
				.isInstanceOf(ResponseStatusException.class).hasMessageContaining("409")
				.hasMessageContaining("already exists");
	}

	@Test
	void shouldNotListInactivatedWorkplace() {
		UUID clinicId = UUID.randomUUID();

		WorkplaceDto created = workplaceService
				.create(new WorkplaceCreateDto(clinicId, "Consultório Inativo", "Será inativado"));

		workplaceService.delete(created.id());

		List<WorkplaceDto> activeList = workplaceService.findAllByClinic(clinicId);
		assertThat(activeList).noneMatch(w -> w.id().equals(created.id()));
	}

	@Test
	void shouldFailWhenWorkplaceNotFound() {
		UUID nonExistentId = UUID.randomUUID();

		assertThatThrownBy(() -> workplaceService.findById(nonExistentId)).isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("404").hasMessageContaining("not found");
	}

	@Test
	void shouldFailWhenUpdateNonExistentWorkplace() {
		UUID nonExistentId = UUID.randomUUID();

		assertThatThrownBy(
				() -> workplaceService.update(nonExistentId, new WorkplaceUpdateDto("New Name", "New Description")))
				.isInstanceOf(ResponseStatusException.class).hasMessageContaining("404");
	}

	@Test
	void shouldAllowSameNameInDifferentClinics() {
		UUID clinic1 = UUID.randomUUID();
		UUID clinic2 = UUID.randomUUID();
		String sameName = "Consultório";

		WorkplaceDto wp1 = workplaceService.create(new WorkplaceCreateDto(clinic1, sameName, "Clínica 1"));

		WorkplaceDto wp2 = workplaceService.create(new WorkplaceCreateDto(clinic2, sameName, "Clínica 2"));

		assertThat(wp1.id()).isNotEqualTo(wp2.id());
		assertThat(wp1.clinicId()).isNotEqualTo(wp2.clinicId());
	}
}
