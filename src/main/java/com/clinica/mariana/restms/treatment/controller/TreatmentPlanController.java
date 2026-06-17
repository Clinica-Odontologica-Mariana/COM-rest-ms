package com.clinica.mariana.restms.treatment.controller;

import com.clinica.mariana.restms.treatment.dto.TreatmentPlanCreateDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemCreateDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemUpdateDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanUpdateDto;
import com.clinica.mariana.restms.treatment.service.TreatmentPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/treatment-plans")
@Tag(name = "Treatment Plans", description = "Gestão de planos de tratamento odontológico")
public class TreatmentPlanController {

	private final TreatmentPlanService service;

	public TreatmentPlanController(TreatmentPlanService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	@Operation(summary = "Criar plano de tratamento")
	@ApiResponses({@ApiResponse(responseCode = "201", description = "Plano criado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Paciente, prontuário ou profissional não encontrado"),
			@ApiResponse(responseCode = "409", description = "Prontuário não pertence ao paciente informado")})
	public TreatmentPlanDto create(@Valid @RequestBody TreatmentPlanCreateDto request) {
		return service.create(request);
	}

	@GetMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	@Operation(summary = "Buscar plano por ID")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "Plano encontrado"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Plano não encontrado")})
	public TreatmentPlanDto findById(@Parameter(description = "ID do plano") @PathVariable UUID id) {
		return service.findById(id);
	}

	@GetMapping("/by-patient/{patientId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	@Operation(summary = "Listar planos por paciente", description = "Retorna todos os planos do paciente, ordenados por data de criação decrescente")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Paciente não encontrado")})
	public List<TreatmentPlanDto> findByPatient(
			@Parameter(description = "ID do paciente") @PathVariable UUID patientId) {
		return service.findByPatient(patientId);
	}

	@PutMapping("/{id}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	@Operation(summary = "Atualizar plano de tratamento")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "Plano atualizado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Plano ou profissional não encontrado")})
	public TreatmentPlanDto update(@Parameter(description = "ID do plano") @PathVariable UUID id,
			@Valid @RequestBody TreatmentPlanUpdateDto request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	@Operation(summary = "Cancelar plano de tratamento", description = "Marca o plano como CANCELLED (soft delete)")
	@ApiResponses({@ApiResponse(responseCode = "204", description = "Plano cancelado com sucesso"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Plano não encontrado")})
	public void delete(@Parameter(description = "ID do plano") @PathVariable UUID id) {
		service.delete(id);
	}

	@PostMapping("/{planId}/items")
	@ResponseStatus(HttpStatus.CREATED)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	@Operation(summary = "Adicionar procedimento ao plano", description = "Cria um item com descrição, categoria, dente, preço estimado e lista de materiais")
	@ApiResponses({@ApiResponse(responseCode = "201", description = "Item criado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou número de dente inválido"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Plano ou procedimento clínico não encontrado")})
	public TreatmentPlanItemDto addItem(@Parameter(description = "ID do plano") @PathVariable UUID planId,
			@Valid @RequestBody TreatmentPlanItemCreateDto request) {
		return service.addItem(planId, request);
	}

	@GetMapping("/{planId}/items")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	@Operation(summary = "Listar procedimentos do plano", description = "Retorna os itens ordenados por sortOrder e data de criação, incluindo materiais de cada item")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Plano não encontrado")})
	public List<TreatmentPlanItemDto> findItems(@Parameter(description = "ID do plano") @PathVariable UUID planId) {
		return service.findItems(planId);
	}

	@PutMapping("/items/{itemId}")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	@Operation(summary = "Atualizar procedimento", description = "Atualiza os dados do item e substitui completamente a lista de materiais")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "Item atualizado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou número de dente inválido"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Item ou procedimento clínico não encontrado")})
	public TreatmentPlanItemDto updateItem(@Parameter(description = "ID do item") @PathVariable UUID itemId,
			@Valid @RequestBody TreatmentPlanItemUpdateDto request) {
		return service.updateItem(itemId, request);
	}

	@PatchMapping("/items/{itemId}/complete")
	@RolesAllowed({"ADMIN", "DOCTOR"})
	@Operation(summary = "Concluir procedimento", description = "Marca o item como DONE e registra a data de conclusão. Idempotente: retorna 200 mesmo que o item já esteja concluído")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "Item concluído (ou já estava concluído)"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Item não encontrado")})
	public TreatmentPlanItemDto completeItem(@Parameter(description = "ID do item") @PathVariable UUID itemId) {
		return service.completeItem(itemId);
	}

	@DeleteMapping("/items/{itemId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RolesAllowed({"ADMIN", "DOCTOR"})
	@Operation(summary = "Cancelar procedimento", description = "Marca o item como CANCELLED (soft delete)")
	@ApiResponses({@ApiResponse(responseCode = "204", description = "Item cancelado com sucesso"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Sem permissão"),
			@ApiResponse(responseCode = "404", description = "Item não encontrado")})
	public void deleteItem(@Parameter(description = "ID do item") @PathVariable UUID itemId) {
		service.deleteItem(itemId);
	}
}
