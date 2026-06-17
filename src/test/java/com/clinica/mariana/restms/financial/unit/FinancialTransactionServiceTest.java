package com.clinica.mariana.restms.financial.unit;

import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.financial.dto.FinancialTransactionCreateDto;
import com.clinica.mariana.restms.financial.entity.FinancialTransactionEntity;
import com.clinica.mariana.restms.financial.repository.FinancialTransactionRepository;
import com.clinica.mariana.restms.financial.service.FinancialTransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Financial transaction service")
class FinancialTransactionServiceTest {

	@Mock
	private FinancialTransactionRepository repository;

	@Mock
	private ClinicRepository clinicRepository;

	@InjectMocks
	private FinancialTransactionService service;

	@Test
	@DisplayName("Should create transaction when clinic exists")
	void shouldCreateTransaction() {
		UUID clinicId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		FinancialTransactionCreateDto dto = new FinancialTransactionCreateDto(clinicId, null, null,
				"Consulta Odontológica", "RECEITA", "CONSULTA", new BigDecimal("250.00"), "PAID", LocalDate.now(),
				"Pagamento em PIX");

		when(clinicRepository.existsById(clinicId)).thenReturn(true);
		when(repository.save(any(FinancialTransactionEntity.class))).thenAnswer(invocation -> {
			FinancialTransactionEntity entity = invocation.getArgument(0);
			entity.setId(UUID.randomUUID());
			return entity;
		});

		var result = service.create(dto, userId);

		assertThat(result.id()).isNotNull();
		assertThat(result.clinicId()).isEqualTo(clinicId);
		assertThat(result.description()).isEqualTo("Consulta Odontológica");
		assertThat(result.amount()).isEqualTo(new BigDecimal("250.00"));
		assertThat(result.status()).isEqualTo("PAID");
		assertThat(result.createdByUserId()).isEqualTo(userId);
	}

	@Test
	@DisplayName("Should throw exception on create when clinic is not found")
	void shouldRejectCreateWhenClinicNotFound() {
		UUID clinicId = UUID.randomUUID();
		FinancialTransactionCreateDto dto = new FinancialTransactionCreateDto(clinicId, null, null, "Desc", "RECEITA",
				"CAT", BigDecimal.TEN, "PENDING", LocalDate.now(), "");

		when(clinicRepository.existsById(clinicId)).thenReturn(false);

		assertThatThrownBy(() -> service.create(dto, UUID.randomUUID())).isInstanceOf(AppException.class)
				.hasMessageContaining("Clinic not found");
	}

	@Test
	@DisplayName("Should logical delete transaction by changing status to CANCELLED")
	void shouldDeleteTransaction() {
		UUID id = UUID.randomUUID();
		FinancialTransactionEntity entity = new FinancialTransactionEntity();
		entity.setId(id);
		entity.setStatus("PENDING");

		when(repository.findById(id)).thenReturn(Optional.of(entity));

		service.delete(id);

		assertThat(entity.getStatus()).isEqualTo("CANCELLED");
		verify(repository).save(entity);
	}
}
