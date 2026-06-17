package com.clinica.mariana.restms.financial.controller;

import com.clinica.mariana.restms.financial.dto.FinancialTransactionCreateDto;
import com.clinica.mariana.restms.financial.dto.FinancialTransactionDto;
import com.clinica.mariana.restms.financial.dto.FinancialTransactionUpdateDto;
import com.clinica.mariana.restms.financial.dto.MonthlyTrendDto;
import com.clinica.mariana.restms.financial.dto.RevenueByServiceDto;
import com.clinica.mariana.restms.financial.service.FinancialTransactionService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/financial-transactions")
public class FinancialTransactionController {

	private final FinancialTransactionService service;

	public FinancialTransactionController(FinancialTransactionService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public FinancialTransactionDto create(@Valid @RequestBody FinancialTransactionCreateDto request,
			@AuthenticationPrincipal Jwt jwt) {

		return service.create(request);
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public FinancialTransactionDto findById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
		return service.findById(id);
	}

	@GetMapping("/by-clinic/{clinicId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public List<FinancialTransactionDto> findByClinic(@PathVariable UUID clinicId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
			@AuthenticationPrincipal Jwt jwt) {

		if (start != null && end != null) {
			return service.findByClinicAndPeriod(clinicId, start, end);
		}
		return service.findByClinic(clinicId);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public FinancialTransactionDto update(@PathVariable UUID id,
			@Valid @RequestBody FinancialTransactionUpdateDto request, @AuthenticationPrincipal Jwt jwt) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public void delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
		service.delete(id);
	}

	@GetMapping("/dashboard/{clinicId}/monthly-trend")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public List<MonthlyTrendDto> monthlyTrend(@PathVariable UUID clinicId, @RequestParam(defaultValue = "6") int months,
			@AuthenticationPrincipal Jwt jwt) {
		return service.getMonthlyTrend(clinicId, months);
	}

	@GetMapping("/dashboard/{clinicId}/revenue-by-service")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	public List<RevenueByServiceDto> revenueByService(@PathVariable UUID clinicId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
			@AuthenticationPrincipal Jwt jwt) {
		return service.getRevenueByService(clinicId, start, end);
	}

	private Optional<UUID> currentUserId(Jwt jwt) {
		if (jwt == null) {
			return Optional.empty();
		}

		String appUserId = jwt.getClaimAsString("app_user_id");
		if (appUserId != null && !appUserId.isBlank()) {
			return parseUuid(appUserId);
		}

		return parseUuid(jwt.getSubject());
	}

	private Optional<UUID> parseUuid(String value) {
		try {
			return value == null || value.isBlank() ? Optional.empty() : Optional.of(UUID.fromString(value));
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}
}
