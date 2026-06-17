package com.clinica.mariana.restms.financial.service;

import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.financial.dto.FinancialTransactionCreateDto;
import com.clinica.mariana.restms.financial.dto.FinancialTransactionDto;
import com.clinica.mariana.restms.financial.dto.FinancialTransactionUpdateDto;
import com.clinica.mariana.restms.financial.dto.MonthlyTrendDto;
import com.clinica.mariana.restms.financial.dto.RevenueByServiceDto;
import com.clinica.mariana.restms.financial.entity.FinancialTransactionEntity;
import com.clinica.mariana.restms.financial.repository.FinancialTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class FinancialTransactionService {

	private static final String NOT_FOUND_MSG = "Financial transaction not found";
	private static final String NOT_FOUND_CODE = "FINANCIAL_TRANSACTION_NOT_FOUND";

	private final FinancialTransactionRepository repository;
	private final ClinicRepository clinicRepository;

	public FinancialTransactionService(FinancialTransactionRepository repository, ClinicRepository clinicRepository) {
		this.repository = repository;
		this.clinicRepository = clinicRepository;
	}

	@Transactional
	public FinancialTransactionDto create(FinancialTransactionCreateDto request, UUID userId) {
		validateClinic(request.clinicId());
		FinancialTransactionEntity entity = new FinancialTransactionEntity();

		entity.setCreatedByUserId(userId);

		entity.setClinicId(request.clinicId());
		entity.setAppointmentId(request.appointmentId());
		entity.setTreatmentPlanId(request.treatmentPlanId());
		entity.setDescription(request.description());
		entity.setType(request.type());
		entity.setCategory(request.category());
		entity.setAmount(request.amount());
		entity.setStatus(defaultIfBlank(request.status(), "PENDING"));
		entity.setTransactionDate(request.transactionDate());
		entity.setNotes(request.notes());

		return toDto(repository.save(entity));
	}

	@Transactional(readOnly = true)
	public FinancialTransactionDto findById(UUID id) {
		return toDto(findEntity(id));
	}

	@Transactional(readOnly = true)
	public List<FinancialTransactionDto> findByClinic(UUID clinicId) {
		validateClinic(clinicId);
		return repository.findAllByClinicIdOrderByTransactionDateDescCreatedAtDesc(clinicId).stream().map(this::toDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<FinancialTransactionDto> findByClinicAndPeriod(UUID clinicId, LocalDate start, LocalDate end) {
		validateClinic(clinicId);
		return repository.findAllByClinicIdAndTransactionDateBetweenOrderByTransactionDateDesc(clinicId, start, end)
				.stream().map(this::toDto).toList();
	}

	@Transactional
	public FinancialTransactionDto update(UUID id, FinancialTransactionUpdateDto request) {
		FinancialTransactionEntity entity = findEntity(id);
		entity.setDescription(request.description());
		entity.setType(request.type());
		entity.setCategory(request.category());
		entity.setAmount(request.amount());
		entity.setStatus(defaultIfBlank(request.status(), entity.getStatus()));
		entity.setTransactionDate(request.transactionDate());
		entity.setNotes(request.notes());
		return toDto(repository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		FinancialTransactionEntity entity = findEntity(id);
		entity.setStatus("CANCELLED");
		repository.save(entity);
	}

	@Transactional(readOnly = true)
	public List<MonthlyTrendDto> getMonthlyTrend(UUID clinicId, int months) {
		validateClinic(clinicId);
		LocalDate end = LocalDate.now();
		LocalDate start = end.minusMonths(months - 1L).withDayOfMonth(1);

		List<Object[]> rows = repository.findMonthlyTrend(clinicId, start, end);

		Map<String, BigDecimal[]> map = new HashMap<>();
		for (Object[] row : rows) {
			int year = ((Number) row[0]).intValue();
			int month = ((Number) row[1]).intValue();
			String type = (String) row[2];
			BigDecimal total = (BigDecimal) row[3];
			String key = year + "-" + month;
			map.putIfAbsent(key, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
			if ("RECEITA".equals(type)) {
				map.get(key)[0] = total;
			} else {
				map.get(key)[1] = total;
			}
		}

		List<MonthlyTrendDto> result = new ArrayList<>();
		LocalDate cursor = start;
		while (!cursor.isAfter(end)) {
			String key = cursor.getYear() + "-" + cursor.getMonthValue();
			BigDecimal[] vals = map.getOrDefault(key, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
			String label = Month.of(cursor.getMonthValue()).getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"))
					.toUpperCase();
			result.add(new MonthlyTrendDto(cursor.getYear(), cursor.getMonthValue(), label, vals[0], vals[1]));
			cursor = cursor.plusMonths(1);
		}
		return result;
	}

	@Transactional(readOnly = true)
	public List<RevenueByServiceDto> getRevenueByService(UUID clinicId, LocalDate start, LocalDate end) {
		validateClinic(clinicId);
		return repository.findRevenueByCategory(clinicId, start, end).stream()
				.map(row -> new RevenueByServiceDto(row[0] != null ? (String) row[0] : "Outros", (BigDecimal) row[1]))
				.toList();
	}

	private FinancialTransactionEntity findEntity(UUID id) {
		return repository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, NOT_FOUND_CODE, NOT_FOUND_MSG));
	}

	private void validateClinic(UUID clinicId) {
		if (!clinicRepository.existsById(clinicId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "CLINIC_NOT_FOUND", "Clinic not found");
		}
	}

	private String defaultIfBlank(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}

	private FinancialTransactionDto toDto(FinancialTransactionEntity e) {
		return new FinancialTransactionDto(e.getId(), e.getClinicId(), e.getAppointmentId(), e.getTreatmentPlanId(),
				e.getDescription(), e.getType(), e.getCategory(), e.getAmount(), e.getStatus(), e.getTransactionDate(),
				e.getNotes(), e.getCreatedByUserId(), e.getCreatedAt(), e.getUpdatedAt());
	}
}
