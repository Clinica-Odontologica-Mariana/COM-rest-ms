package com.clinica.mariana.restms.treatment.integration;

import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordEntity;
import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordRepository;
import com.clinica.mariana.restms.patient.entity.PatientEntity;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import com.clinica.mariana.restms.treatment.dto.MaterialItemCreateDto;
import com.clinica.mariana.restms.treatment.dto.MaterialItemDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanCreateDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemCreateDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemUpdateDto;
import com.clinica.mariana.restms.treatment.repository.TreatmentPlanItemMaterialRepository;
import com.clinica.mariana.restms.treatment.repository.TreatmentPlanItemRepository;
import com.clinica.mariana.restms.treatment.repository.TreatmentPlanRepository;
import com.clinica.mariana.restms.treatment.service.TreatmentPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("TreatmentPlanService — integration")
class TreatmentPlanServiceIntegrationTest {

	@Autowired
	private TreatmentPlanService service;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private MedicalRecordRepository medicalRecordRepository;

	@Autowired
	private TreatmentPlanRepository planRepository;

	@Autowired
	private TreatmentPlanItemRepository itemRepository;

	@Autowired
	private TreatmentPlanItemMaterialRepository materialRepository;

	private UUID patientId;
	private UUID recordId;

	@BeforeEach
	void seedPatientAndRecord() {
		PatientEntity patient = new PatientEntity();
		patient.setFullName("Paciente Plano");
		patient.setCpf(randomCpf());
		patient.setPhone("61999999999");
		patient.setBirthDate(LocalDate.of(1990, 5, 15));
		patient.setActive(true);
		patient = patientRepository.save(patient);
		patientId = patient.getId();

		MedicalRecordEntity record = new MedicalRecordEntity();
		record.setPatientId(patientId);
		record = medicalRecordRepository.save(record);
		recordId = record.getId();
	}

	private TreatmentPlanDto createPlan() {
		return service
				.create(new TreatmentPlanCreateDto(patientId, recordId, null, "Plano Teste", "DRAFT", null, null));
	}

	@Nested
	@DisplayName("Category field")
	class CategoryField {

		@Test
		@DisplayName("Persists category when creating an item")
		void shouldPersistCategory() {
			TreatmentPlanDto plan = createPlan();
			TreatmentPlanItemCreateDto request = new TreatmentPlanItemCreateDto(null, 11, "Restauração", "Restauração",
					BigDecimal.valueOf(350), "PENDING", 1, null);

			TreatmentPlanItemDto item = service.addItem(plan.id(), request);

			assertThat(item.category()).isEqualTo("Restauração");

			List<TreatmentPlanItemDto> items = service.findItems(plan.id());
			assertThat(items).filteredOn(i -> i.id().equals(item.id())).singleElement()
					.satisfies(i -> assertThat(i.category()).isEqualTo("Restauração"));
		}

		@Test
		@DisplayName("Updates category when editing an item")
		void shouldUpdateCategory() {
			TreatmentPlanDto plan = createPlan();
			TreatmentPlanItemDto item = service.addItem(plan.id(), new TreatmentPlanItemCreateDto(null, 21, "Extração",
					"Cirurgia", BigDecimal.valueOf(400), "PENDING", 1, null));

			TreatmentPlanItemDto updated = service.updateItem(item.id(), new TreatmentPlanItemUpdateDto(null, 21,
					"Extração de Siso", "Endodontia", BigDecimal.valueOf(500), "APPROVED", 1, null));

			assertThat(updated.category()).isEqualTo("Endodontia");
		}

		@Test
		@DisplayName("Allows null category")
		void shouldAllowNullCategory() {
			TreatmentPlanDto plan = createPlan();
			TreatmentPlanItemCreateDto request = new TreatmentPlanItemCreateDto(null, 11, "Limpeza", null,
					BigDecimal.valueOf(100), "PENDING", 1, null);

			TreatmentPlanItemDto item = service.addItem(plan.id(), request);

			assertThat(item.category()).isNull();
		}
	}

	@Nested
	@DisplayName("Materials field")
	class MaterialsField {

		@Test
		@DisplayName("Persists materials when creating an item")
		void shouldPersistMaterials() {
			TreatmentPlanDto plan = createPlan();
			List<MaterialItemCreateDto> materials = List.of(
					new MaterialItemCreateDto("Resina Composta", "Restauração", 2),
					new MaterialItemCreateDto("Broca Diamantada", null, 1));

			TreatmentPlanItemDto item = service.addItem(plan.id(), new TreatmentPlanItemCreateDto(null, 11,
					"Restauração", "Restauração", BigDecimal.valueOf(300), "PENDING", 1, materials));

			assertThat(item.materials()).hasSize(2);
			assertThat(item.materials()).extracting(MaterialItemDto::name).containsExactlyInAnyOrder("Resina Composta",
					"Broca Diamantada");
			assertThat(item.materials()).filteredOn(m -> "Resina Composta".equals(m.name())).singleElement()
					.satisfies(m -> {
						assertThat(m.quantity()).isEqualTo(2);
						assertThat(m.category()).isEqualTo("Restauração");
					});
		}

		@Test
		@DisplayName("Materials are loaded when listing items")
		void shouldLoadMaterialsOnFindItems() {
			TreatmentPlanDto plan = createPlan();
			service.addItem(plan.id(),
					new TreatmentPlanItemCreateDto(null, 11, "Restauração", "Restauração", BigDecimal.valueOf(300),
							"PENDING", 1, List.of(new MaterialItemCreateDto("Resina", "Restauração", 1))));

			List<TreatmentPlanItemDto> items = service.findItems(plan.id());

			assertThat(items).hasSize(1);
			assertThat(items.getFirst().materials()).hasSize(1);
			assertThat(items.getFirst().materials().getFirst().name()).isEqualTo("Resina");
		}

		@Test
		@DisplayName("Materials are fully replaced on update")
		void shouldReplaceMaterialsOnUpdate() {
			TreatmentPlanDto plan = createPlan();
			TreatmentPlanItemDto item = service.addItem(plan.id(),
					new TreatmentPlanItemCreateDto(null, 11, "Restauração", "Restauração", BigDecimal.valueOf(300),
							"PENDING", 1, List.of(new MaterialItemCreateDto("Resina Antiga", "Restauração", 1))));

			TreatmentPlanItemDto updated = service.updateItem(item.id(),
					new TreatmentPlanItemUpdateDto(null, 11, "Restauração", "Restauração", BigDecimal.valueOf(350),
							"APPROVED", 1, List.of(new MaterialItemCreateDto("Resina Nova", "Restauração", 3),
									new MaterialItemCreateDto("Adesivo", "Restauração", 1))));

			assertThat(updated.materials()).hasSize(2);
			assertThat(updated.materials()).extracting(MaterialItemDto::name)
					.containsExactlyInAnyOrder("Resina Nova", "Adesivo").doesNotContain("Resina Antiga");

			long dbCount = materialRepository.findAllByItemId(item.id()).size();
			assertThat(dbCount).isEqualTo(2);
		}

		@Test
		@DisplayName("Materials are cleared when update sends empty list")
		void shouldClearMaterialsOnUpdateWithEmptyList() {
			TreatmentPlanDto plan = createPlan();
			TreatmentPlanItemDto item = service.addItem(plan.id(),
					new TreatmentPlanItemCreateDto(null, 11, "Restauração", "Restauração", BigDecimal.valueOf(300),
							"PENDING", 1, List.of(new MaterialItemCreateDto("Resina", "Restauração", 1))));

			TreatmentPlanItemDto updated = service.updateItem(item.id(), new TreatmentPlanItemUpdateDto(null, 11,
					"Restauração", "Restauração", BigDecimal.valueOf(300), "PENDING", 1, List.of()));

			assertThat(updated.materials()).isEmpty();
			assertThat(materialRepository.findAllByItemId(item.id())).isEmpty();
		}

		@Test
		@DisplayName("Materials persist after soft-delete (status CANCELLED), pois deleteItem é soft-delete")
		void shouldKeepMaterialsAfterSoftDelete() {
			TreatmentPlanDto plan = createPlan();
			TreatmentPlanItemDto item = service.addItem(plan.id(),
					new TreatmentPlanItemCreateDto(null, 11, "Restauração", "Restauração", BigDecimal.valueOf(300),
							"PENDING", 1, List.of(new MaterialItemCreateDto("Resina", "Restauração", 1))));

			service.deleteItem(item.id());

			// O item recebe status CANCELLED (soft delete); materiais permanecem no banco
			assertThat(materialRepository.findAllByItemId(item.id())).hasSize(1);
		}
	}

	@Nested
	@DisplayName("Idempotent /complete")
	class CompleteIdempotency {

		@Test
		@DisplayName("Returns 200 and DONE status on first call")
		void shouldCompleteItem() {
			TreatmentPlanDto plan = createPlan();
			TreatmentPlanItemDto item = service.addItem(plan.id(), new TreatmentPlanItemCreateDto(null, 11,
					"Restauração", "Restauração", BigDecimal.valueOf(300), "PENDING", 1, null));

			TreatmentPlanItemDto result = service.completeItem(item.id());

			assertThat(result.status()).isEqualTo("DONE");
			assertThat(result.completedAt()).isNotNull();
		}

		@Test
		@DisplayName("Second call returns DONE without error (idempotent)")
		void shouldBeIdempotentOnSecondCall() {
			TreatmentPlanDto plan = createPlan();
			TreatmentPlanItemDto item = service.addItem(plan.id(), new TreatmentPlanItemCreateDto(null, 11,
					"Restauração", "Restauração", BigDecimal.valueOf(300), "PENDING", 1, null));

			TreatmentPlanItemDto first = service.completeItem(item.id());
			TreatmentPlanItemDto second = service.completeItem(item.id());

			assertThat(second.status()).isEqualTo("DONE");
			// completedAt deve permanecer o mesmo (sem nova escrita no banco)
			assertThat(second.completedAt()).isNotNull();
			assertThat(second.completedAt().toEpochSecond()).isEqualTo(first.completedAt().toEpochSecond());
		}

		@Test
		@DisplayName("Repeated completions preserve materials")
		void shouldPreserveMaterialsOnIdempotentComplete() {
			TreatmentPlanDto plan = createPlan();
			TreatmentPlanItemDto item = service.addItem(plan.id(),
					new TreatmentPlanItemCreateDto(null, 11, "Restauração", "Restauração", BigDecimal.valueOf(300),
							"PENDING", 1, List.of(new MaterialItemCreateDto("Resina", "Restauração", 1))));

			service.completeItem(item.id());
			TreatmentPlanItemDto second = service.completeItem(item.id());

			assertThat(second.materials()).hasSize(1);
			assertThat(second.materials().getFirst().name()).isEqualTo("Resina");
		}
	}

	@Nested
	@DisplayName("Error scenarios")
	class ErrorScenarios {

		@Test
		@DisplayName("Adding item to non-existent plan throws 404")
		void shouldThrow404ForUnknownPlan() {
			TreatmentPlanItemCreateDto request = new TreatmentPlanItemCreateDto(null, 11, "Restauração", null,
					BigDecimal.valueOf(300), "PENDING", 1, null);

			assertStatus(HttpStatus.NOT_FOUND, () -> service.addItem(UUID.randomUUID(), request));
		}

		@Test
		@DisplayName("Completing non-existent item throws 404")
		void shouldThrow404ForUnknownItem() {
			assertStatus(HttpStatus.NOT_FOUND, () -> service.completeItem(UUID.randomUUID()));
		}

		@Test
		@DisplayName("Invalid tooth number throws 400")
		void shouldThrow400ForInvalidTooth() {
			TreatmentPlanDto plan = createPlan();
			TreatmentPlanItemCreateDto request = new TreatmentPlanItemCreateDto(null, 99, "Restauração", null,
					BigDecimal.valueOf(300), "PENDING", 1, null);

			assertStatus(HttpStatus.BAD_REQUEST, () -> service.addItem(plan.id(), request));
		}
	}

	private void assertStatus(HttpStatus status, Runnable action) {
		assertThatThrownBy(action::run).isInstanceOf(AppException.class)
				.satisfies(e -> assertThat(((AppException) e).getStatus()).isEqualTo(status));
	}

	private String randomCpf() {
		return String.valueOf(10000000000L + (long) (Math.random() * 89999999999L));
	}
}
