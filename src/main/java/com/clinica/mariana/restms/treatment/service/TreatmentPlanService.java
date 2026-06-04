package com.clinica.mariana.restms.treatment.service;

import com.clinica.mariana.restms.clinicalprocedure.repository.ClinicalProcedureRepository;
import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordEntity;
import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordRepository;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanCreateDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemCreateDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanItemUpdateDto;
import com.clinica.mariana.restms.treatment.dto.TreatmentPlanUpdateDto;
import com.clinica.mariana.restms.treatment.entity.TreatmentPlanEntity;
import com.clinica.mariana.restms.treatment.entity.TreatmentPlanItemEntity;
import com.clinica.mariana.restms.treatment.repository.TreatmentPlanItemRepository;
import com.clinica.mariana.restms.treatment.repository.TreatmentPlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clinica.mariana.restms.common.exception.AppException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TreatmentPlanService {

	private static final String PLAN_NOT_FOUND = "Treatment plan not found";
	private static final String ITEM_NOT_FOUND = "Treatment plan item not found";

	private final TreatmentPlanRepository planRepository;
	private final TreatmentPlanItemRepository itemRepository;
	private final PatientRepository patientRepository;
	private final MedicalRecordRepository medicalRecordRepository;
	private final ProfessionalRepository professionalRepository;
	private final ClinicalProcedureRepository procedureRepository;

	public TreatmentPlanService(TreatmentPlanRepository planRepository, TreatmentPlanItemRepository itemRepository,
			PatientRepository patientRepository, MedicalRecordRepository medicalRecordRepository,
			ProfessionalRepository professionalRepository, ClinicalProcedureRepository procedureRepository) {
		this.planRepository = planRepository;
		this.itemRepository = itemRepository;
		this.patientRepository = patientRepository;
		this.medicalRecordRepository = medicalRecordRepository;
		this.professionalRepository = professionalRepository;
		this.procedureRepository = procedureRepository;
	}

	@Transactional
	public TreatmentPlanDto create(TreatmentPlanCreateDto request) {
		validatePatientAndRecord(request.patientId(), request.medicalRecordId());
		validateProfessional(request.professionalId());
		TreatmentPlanEntity entity = new TreatmentPlanEntity();
		entity.setPatientId(request.patientId());
		entity.setMedicalRecordId(request.medicalRecordId());
		entity.setProfessionalId(request.professionalId());
		entity.setTitle(request.title());
		entity.setStatus(defaultIfBlank(request.status(), "DRAFT"));
		entity.setNotes(request.notes());
		entity.setTotalAmount(request.totalAmount());
		return toDto(planRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public TreatmentPlanDto findById(UUID id) {
		return toDto(findPlan(id));
	}

	@Transactional(readOnly = true)
	public List<TreatmentPlanDto> findByPatient(UUID patientId) {
		if (!patientRepository.existsById(patientId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", "Patient not found");
		}
		return planRepository.findAllByPatientIdOrderByCreatedAtDesc(patientId).stream().map(this::toDto).toList();
	}

	@Transactional
	public TreatmentPlanDto update(UUID id, TreatmentPlanUpdateDto request) {
		TreatmentPlanEntity entity = findPlan(id);
		validateProfessional(request.professionalId());
		entity.setProfessionalId(request.professionalId());
		entity.setTitle(request.title());
		entity.setStatus(defaultIfBlank(request.status(), entity.getStatus()));
		entity.setNotes(request.notes());
		entity.setTotalAmount(request.totalAmount());
		return toDto(planRepository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		TreatmentPlanEntity entity = findPlan(id);
		entity.setStatus("CANCELLED");
		planRepository.save(entity);
	}

	@Transactional
	public TreatmentPlanItemDto addItem(UUID planId, TreatmentPlanItemCreateDto request) {
		findPlan(planId);
		validateProcedure(request.procedureId());
		validateTooth(request.toothNumber());
		TreatmentPlanItemEntity entity = new TreatmentPlanItemEntity();
		entity.setTreatmentPlanId(planId);
		applyItem(entity, request.procedureId(), request.toothNumber(), request.description(), request.estimatedPrice(),
				defaultIfBlank(request.status(), "PENDING"), request.sortOrder());
		return toDto(itemRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public List<TreatmentPlanItemDto> findItems(UUID planId) {
		findPlan(planId);
		return itemRepository.findAllByTreatmentPlanIdOrderBySortOrderAscCreatedAtAsc(planId).stream().map(this::toDto)
				.toList();
	}

	@Transactional
	public TreatmentPlanItemDto updateItem(UUID itemId, TreatmentPlanItemUpdateDto request) {
		TreatmentPlanItemEntity entity = findItem(itemId);
		validateProcedure(request.procedureId());
		validateTooth(request.toothNumber());
		applyItem(entity, request.procedureId(), request.toothNumber(), request.description(), request.estimatedPrice(),
				defaultIfBlank(request.status(), entity.getStatus()), request.sortOrder());
		return toDto(itemRepository.save(entity));
	}

	@Transactional
	public TreatmentPlanItemDto completeItem(UUID itemId) {
		TreatmentPlanItemEntity entity = findItem(itemId);
		entity.setStatus("DONE");
		entity.setCompletedAt(OffsetDateTime.now());
		return toDto(itemRepository.save(entity));
	}

	@Transactional
	public void deleteItem(UUID itemId) {
		TreatmentPlanItemEntity entity = findItem(itemId);
		entity.setStatus("CANCELLED");
		itemRepository.save(entity);
	}

	private TreatmentPlanEntity findPlan(UUID id) {
		return planRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "TREATMENT_PLAN_NOT_FOUND", PLAN_NOT_FOUND));
	}

	private TreatmentPlanItemEntity findItem(UUID id) {
		return itemRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "ITEM_NOT_FOUND", ITEM_NOT_FOUND));
	}

	private void validatePatientAndRecord(UUID patientId, UUID medicalRecordId) {
		if (!patientRepository.existsById(patientId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", "Patient not found");
		}
		MedicalRecordEntity record = medicalRecordRepository.findById(medicalRecordId)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "MEDICAL_RECORD_NOT_FOUND", "Medical record not found"));
		if (!record.getPatientId().equals(patientId)) {
			throw new AppException(HttpStatus.CONFLICT, "MEDICAL_RECORD_MISMATCH", "Medical record does not belong to patient");
		}
	}

	private void validateProfessional(UUID professionalId) {
		if (professionalId != null && !professionalRepository.existsById(professionalId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "PROFESSIONAL_NOT_FOUND", "Professional not found");
		}
	}

	private void validateProcedure(UUID procedureId) {
		if (procedureId != null && !procedureRepository.existsById(procedureId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Clinical procedure not found");
		}
	}

	private void validateTooth(Integer toothNumber) {
		if (toothNumber == null) {
			return;
		}
		boolean permanent = toothNumber >= 11 && toothNumber <= 48
				&& !java.util.Set.of(19, 20, 29, 30, 39, 40).contains(toothNumber);
		boolean deciduous = toothNumber >= 51 && toothNumber <= 85
				&& !java.util.Set.of(59, 60, 69, 70, 79, 80).contains(toothNumber);
		if (!permanent && !deciduous) {
			throw new AppException(HttpStatus.BAD_REQUEST, "INVALID_TOOTH_NUMBER", "Invalid tooth number");
		}
	}

	private void applyItem(TreatmentPlanItemEntity entity, UUID procedureId, Integer toothNumber, String description,
			java.math.BigDecimal estimatedPrice, String status, Integer sortOrder) {
		entity.setProcedureId(procedureId);
		entity.setToothNumber(toothNumber);
		entity.setDescription(description);
		entity.setEstimatedPrice(estimatedPrice);
		entity.setStatus(status);
		entity.setSortOrder(sortOrder == null ? 1 : sortOrder);
	}

	private String defaultIfBlank(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}

	private TreatmentPlanDto toDto(TreatmentPlanEntity entity) {
		return new TreatmentPlanDto(entity.getId(), entity.getPatientId(), entity.getMedicalRecordId(),
				entity.getProfessionalId(), entity.getTitle(), entity.getStatus(), entity.getNotes(),
				entity.getTotalAmount(), entity.getCreatedByUserId(), entity.getCreatedAt(), entity.getUpdatedAt());
	}

	private TreatmentPlanItemDto toDto(TreatmentPlanItemEntity entity) {
		return new TreatmentPlanItemDto(entity.getId(), entity.getTreatmentPlanId(), entity.getProcedureId(),
				entity.getToothNumber(), entity.getDescription(), entity.getEstimatedPrice(), entity.getStatus(),
				entity.getSortOrder(), entity.getCompletedAt(), entity.getCreatedAt());
	}
}
