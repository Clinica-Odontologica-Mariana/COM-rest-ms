package com.clinica.mariana.restms.financial.dto;

import java.math.BigDecimal;

public record MonthlyTrendDto(int year, int month, String monthLabel, BigDecimal totalReceita,
		BigDecimal totalDespesa) {
}
