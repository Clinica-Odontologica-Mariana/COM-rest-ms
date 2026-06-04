package com.clinica.mariana.restms.treatment.repository;

import com.clinica.mariana.restms.treatment.entity.TreatmentPlanItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TreatmentPlanItemRepository extends JpaRepository<TreatmentPlanItemEntity, UUID> {

	List<TreatmentPlanItemEntity> findAllByTreatmentPlanIdOrderBySortOrderAscCreatedAtAsc(UUID treatmentPlanId);
}
