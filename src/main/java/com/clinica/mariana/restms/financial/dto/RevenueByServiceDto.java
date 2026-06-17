package com.clinica.mariana.restms.financial.dto;

import java.math.BigDecimal;

public record RevenueByServiceDto(String category, BigDecimal total) {
}
