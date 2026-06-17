package com.clinica.mariana.restms.appointment.controller;

import com.clinica.mariana.restms.appointment.dto.AppointmentCreateDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentDto;
import com.clinica.mariana.restms.appointment.dto.AppointmentUpdateDto;
import com.clinica.mariana.restms.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Appointments", description = "Gestão de consultas")
public class AppointmentController {

	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	@Operation(summary = "Criar consulta", description = "Cria uma nova consulta e sincroniza com o Google Calendar se disponível")
	@ApiResponses({@ApiResponse(responseCode = "201", description = "Consulta criada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Paciente, clínica, profissional ou status não encontrado")})
	public AppointmentDto create(@Valid @RequestBody AppointmentCreateDto request) {
		return appointmentService.create(request);
	}

	@GetMapping
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	@Operation(summary = "Listar consultas", description = "Retorna todas as consultas não canceladas, com paginação")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão")})
	public Page<AppointmentDto> findAll(@PageableDefault(size = 20) Pageable pageable) {
		return appointmentService.findAll(pageable);
	}

	@GetMapping("/period")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	@Operation(summary = "Listar consultas por período", description = "Retorna consultas não canceladas dentro de um intervalo de datas")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Datas inválidas"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão")})
	public Page<AppointmentDto> findByPeriod(@PageableDefault(size = 20) Pageable pageable,
			@Parameter(description = "Data/hora de início (ISO 8601)", example = "2026-06-01T08:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@Parameter(description = "Data/hora de fim (ISO 8601)", example = "2026-06-30T18:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
		return appointmentService.findByPeriod(start.atOffset(ZoneOffset.UTC), end.atOffset(ZoneOffset.UTC), pageable);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR", "RECEPTIONIST"})
	@Operation(summary = "Atualizar consulta")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "Consulta atualizada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Consulta ou status não encontrado")})
	public AppointmentDto update(@Parameter(description = "ID da consulta") @PathVariable UUID id,
			@Valid @RequestBody AppointmentUpdateDto request) {
		return appointmentService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	@Operation(summary = "Cancelar consulta", description = "Marca a consulta como cancelada e remove do Google Calendar se sincronizado")
	@ApiResponses({@ApiResponse(responseCode = "204", description = "Consulta cancelada com sucesso"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Consulta não encontrada")})
	public void delete(@Parameter(description = "ID da consulta") @PathVariable UUID id) {
		appointmentService.delete(id);
	}
}
