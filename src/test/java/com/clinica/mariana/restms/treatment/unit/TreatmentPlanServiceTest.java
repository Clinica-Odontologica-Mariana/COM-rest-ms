package com.clinica.mariana.restms.treatment.unit;

import com.clinica.mariana.restms.clinicalprocedure.repository.ClinicalProcedureRepository;
import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordRepository;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;
import com.clinica.mariana.restms.treatment.dto.MaterialItemCreateDto;
import com.clinica.mariana.restms.treatment.dto.MaterialItemDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemCreateDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemUpdateDto;
import com.clinica.mariana.restms.treatment.entity.TreatmentPlanEntity;
import com.clinica.mariana.restms.treatment.entity.TreatmentPlanItemEntity;
import com.clinica.mariana.restms.treatment.entity.TreatmentPlanItemMaterialEntity;
import com.clinica.mariana.restms.treatment.repository.TreatmentPlanItemMaterialRepository;
import com.clinica.mariana.restms.treatment.repository.TreatmentPlanItemRepository;
import com.clinica.mariana.restms.treatment.repository.TreatmentPlanRepository;
import com.clinica.mariana.restms.treatment.service.TreatmentPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentPlanServiceTest {

	@Mock
	private TreatmentPlanRepository planRepository;

	@Mock
	private TreatmentPlanItemRepository itemRepository;

	@Mock
	private TreatmentPlanItemMaterialRepository materialRepository;

	@Mock
	private PatientRepository patientRepository;

	@Mock
	private MedicalRecordRepository medicalRecordRepository;

	@Mock
	private ProfessionalRepository professionalRepository;

	@Mock
	private ClinicalProcedureRepository procedureRepository;

	@InjectMocks
	private TreatmentPlanService service;

	private UUID planId;
	private UUID itemId;
	private TreatmentPlanEntity plan;
	private TreatmentPlanItemEntity item;

	@BeforeEach
	void setUp() {
		planId = UUID.randomUUID();
		itemId = UUID.randomUUID();

		plan = new TreatmentPlanEntity();
		plan.setId(planId);
		plan.setStatus("DRAFT");

		item = new TreatmentPlanItemEntity();
		item.setId(itemId);
		item.setTreatmentPlanId(planId);
		item.setDescription("Restauração");
		item.setStatus("PENDING");
		item.setSortOrder(1);
	}

	@Nested
	class AddItem {

		@Test
		void shouldPersistCategoryAndMaterials() {
			List<MaterialItemCreateDto> materials = List.of(
					new MaterialItemCreateDto("Resina Composta", "Restauração", 2),
					new MaterialItemCreateDto("Broca Diamantada", null, 1));

			TreatmentPlanItemCreateDto request = new TreatmentPlanItemCreateDto(null, 11, "Restauração", "Restauração",
					BigDecimal.valueOf(300), "PENDING", 1, materials);

			when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
			when(itemRepository.save(any())).thenAnswer(inv -> {
				TreatmentPlanItemEntity e = inv.getArgument(0);
				e.setId(itemId);
				return e;
			});
			when(materialRepository.saveAll(any())).thenAnswer(inv -> {
				List<TreatmentPlanItemMaterialEntity> list = inv.getArgument(0);
				list.forEach(m -> m.setId(UUID.randomUUID()));
				return list;
			});

			TreatmentPlanItemDto result = service.addItem(planId, request);

			assertThat(result.category()).isEqualTo("Restauração");
			assertThat(result.materials()).hasSize(2);
			assertThat(result.materials()).extracting(MaterialItemDto::name)
					.containsExactlyInAnyOrder("Resina Composta", "Broca Diamantada");
			assertThat(result.materials()).filteredOn(m -> "Resina Composta".equals(m.name())).singleElement()
					.satisfies(m -> assertThat(m.quantity()).isEqualTo(2));
		}

		@Test
		void shouldReturnEmptyMaterialsWhenNoneProvided() {
			TreatmentPlanItemCreateDto request = new TreatmentPlanItemCreateDto(null, 11, "Restauração", "Restauração",
					BigDecimal.valueOf(300), "PENDING", 1, null);

			when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
			when(itemRepository.save(any())).thenAnswer(inv -> {
				TreatmentPlanItemEntity e = inv.getArgument(0);
				e.setId(itemId);
				return e;
			});

			TreatmentPlanItemDto result = service.addItem(planId, request);

			assertThat(result.materials()).isEmpty();
			verify(materialRepository, never()).saveAll(any());
		}

		@Test
		void shouldThrow404WhenPlanNotFound() {
			TreatmentPlanItemCreateDto request = new TreatmentPlanItemCreateDto(null, 11, "Restauração", null,
					BigDecimal.valueOf(300), "PENDING", 1, null);

			when(planRepository.findById(planId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.addItem(planId, request)).isInstanceOf(AppException.class)
					.satisfies(e -> assertThat(((AppException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
		}

		@Test
		void shouldThrow400ForInvalidToothNumber() {
			TreatmentPlanItemCreateDto request = new TreatmentPlanItemCreateDto(null, 99, "Restauração", null,
					BigDecimal.valueOf(300), "PENDING", 1, null);

			when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

			assertThatThrownBy(() -> service.addItem(planId, request)).isInstanceOf(AppException.class)
					.satisfies(e -> assertThat(((AppException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
		}

		@Test
		void shouldThrow404ForUnknownProcedure() {
			UUID procedureId = UUID.randomUUID();
			TreatmentPlanItemCreateDto request = new TreatmentPlanItemCreateDto(procedureId, null, "Restauração", null,
					BigDecimal.valueOf(300), "PENDING", 1, null);

			when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
			when(procedureRepository.existsById(procedureId)).thenReturn(false);

			assertThatThrownBy(() -> service.addItem(planId, request)).isInstanceOf(AppException.class)
					.satisfies(e -> assertThat(((AppException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
		}
	}

	@Nested
	class UpdateItem {

		@Test
		void shouldReplaceMaterialsOnUpdate() {
			List<MaterialItemCreateDto> newMaterials = List
					.of(new MaterialItemCreateDto("Cimento de Ionômero", "Selante", 1));

			TreatmentPlanItemUpdateDto request = new TreatmentPlanItemUpdateDto(null, null, "Selamento", "Selamento",
					BigDecimal.valueOf(150), "APPROVED", 2, newMaterials);

			when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
			when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
			when(materialRepository.saveAll(any())).thenAnswer(inv -> {
				List<TreatmentPlanItemMaterialEntity> list = inv.getArgument(0);
				list.forEach(m -> m.setId(UUID.randomUUID()));
				return list;
			});

			TreatmentPlanItemDto result = service.updateItem(itemId, request);

			verify(materialRepository).deleteAllByItemId(itemId);
			assertThat(result.category()).isEqualTo("Selamento");
			assertThat(result.materials()).hasSize(1);
			assertThat(result.materials().getFirst().name()).isEqualTo("Cimento de Ionômero");
		}

		@Test
		void shouldClearMaterialsWhenEmptyListProvided() {
			TreatmentPlanItemUpdateDto request = new TreatmentPlanItemUpdateDto(null, null, "Limpeza", "Prevenção",
					BigDecimal.valueOf(80), "PENDING", 1, Collections.emptyList());

			when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
			when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			TreatmentPlanItemDto result = service.updateItem(itemId, request);

			verify(materialRepository).deleteAllByItemId(itemId);
			assertThat(result.materials()).isEmpty();
		}

		@Test
		void shouldThrow404WhenItemNotFound() {
			TreatmentPlanItemUpdateDto request = new TreatmentPlanItemUpdateDto(null, null, "Limpeza", null, null,
					"PENDING", 1, null);

			when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.updateItem(itemId, request)).isInstanceOf(AppException.class)
					.satisfies(e -> assertThat(((AppException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
		}
	}

	@Nested
	class CompleteItem {

		@Test
		void shouldMarkItemAsDoneAndSetCompletedAt() {
			when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
			when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
			when(materialRepository.findAllByItemId(itemId)).thenReturn(Collections.emptyList());

			TreatmentPlanItemDto result = service.completeItem(itemId);

			assertThat(result.status()).isEqualTo("DONE");
			assertThat(result.completedAt()).isNotNull();
		}

		@Test
		void shouldBeIdempotentWhenItemAlreadyDone() {
			item.setStatus("DONE");
			item.setCompletedAt(OffsetDateTime.now().minusHours(1));

			when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
			when(materialRepository.findAllByItemId(itemId)).thenReturn(Collections.emptyList());

			TreatmentPlanItemDto result = service.completeItem(itemId);

			assertThat(result.status()).isEqualTo("DONE");
			verify(itemRepository, never()).save(any());
		}

		@Test
		void shouldReturnMaterialsOnComplete() {
			TreatmentPlanItemMaterialEntity material = new TreatmentPlanItemMaterialEntity();
			material.setId(UUID.randomUUID());
			material.setItemId(itemId);
			material.setName("Resina");
			material.setCategory("Restauração");
			material.setQuantity(1);

			when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
			when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
			when(materialRepository.findAllByItemId(itemId)).thenReturn(List.of(material));

			TreatmentPlanItemDto result = service.completeItem(itemId);

			assertThat(result.materials()).hasSize(1);
			assertThat(result.materials().getFirst().name()).isEqualTo("Resina");
		}

		@Test
		void shouldThrow404WhenItemNotFound() {
			when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.completeItem(itemId)).isInstanceOf(AppException.class)
					.satisfies(e -> assertThat(((AppException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
		}
	}

	@Nested
	class FindItems {

		@Test
		void shouldLoadMaterialsForEachItem() {
			TreatmentPlanItemEntity item2 = new TreatmentPlanItemEntity();
			item2.setId(UUID.randomUUID());
			item2.setTreatmentPlanId(planId);
			item2.setDescription("Extração");
			item2.setStatus("PENDING");
			item2.setSortOrder(2);

			TreatmentPlanItemMaterialEntity mat = new TreatmentPlanItemMaterialEntity();
			mat.setId(UUID.randomUUID());
			mat.setItemId(itemId);
			mat.setName("Pinça");
			mat.setQuantity(1);

			when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
			when(itemRepository.findAllByTreatmentPlanIdOrderBySortOrderAscCreatedAtAsc(planId))
					.thenReturn(List.of(item, item2));
			when(materialRepository.findAllByItemId(itemId)).thenReturn(List.of(mat));
			when(materialRepository.findAllByItemId(item2.getId())).thenReturn(Collections.emptyList());

			List<TreatmentPlanItemDto> result = service.findItems(planId);

			assertThat(result).hasSize(2);
			assertThat(result.getFirst().materials()).hasSize(1).first()
					.satisfies(m -> assertThat(m.name()).isEqualTo("Pinça"));
			assertThat(result.getLast().materials()).isEmpty();
		}

		@Test
		void shouldThrow404WhenPlanNotFound() {
			when(planRepository.findById(planId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.findItems(planId)).isInstanceOf(AppException.class)
					.satisfies(e -> assertThat(((AppException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
		}
	}
}
