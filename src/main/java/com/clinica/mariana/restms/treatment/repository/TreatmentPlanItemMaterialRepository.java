package com.clinica.mariana.restms.treatment.repository;

import com.clinica.mariana.restms.treatment.entity.TreatmentPlanItemMaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TreatmentPlanItemMaterialRepository extends JpaRepository<TreatmentPlanItemMaterialEntity, UUID> {

	List<TreatmentPlanItemMaterialEntity> findAllByItemId(UUID itemId);

	void deleteAllByItemId(UUID itemId);

}
