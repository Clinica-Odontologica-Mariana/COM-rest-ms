package com.clinica.mariana.restms.treatment.repository;

import com.clinica.mariana.restms.treatment.entity.TreatmentPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlanEntity, UUID> {

	List<TreatmentPlanEntity> findAllByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
