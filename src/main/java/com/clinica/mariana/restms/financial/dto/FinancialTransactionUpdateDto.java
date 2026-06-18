package com.clinica.mariana.restms.financial.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialTransactionUpdateDto(

		@NotBlank(message = "Descrição é obrigatória") @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres") String description,

		@NotNull(message = "Tipo é obrigatório") @Pattern(regexp = "^(RECEITA|DESPESA)$", message = "Tipo deve ser RECEITA ou DESPESA") String type,

		@Size(max = 80, message = "Categoria deve ter no máximo 80 caracteres") String category,

		@NotNull(message = "Valor é obrigatório") @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero") BigDecimal amount,

		@Pattern(regexp = "^(PENDING|PAID|COMPLETED|CANCELLED)$", message = "Status deve ser PENDING, PAID, COMPLETED ou CANCELLED") String status,

		@NotNull(message = "Data da transação é obrigatória") LocalDate transactionDate,

		String notes) {
}
