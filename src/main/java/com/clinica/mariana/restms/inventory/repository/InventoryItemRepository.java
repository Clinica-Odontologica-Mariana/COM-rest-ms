package com.clinica.mariana.restms.inventory.repository;

import com.clinica.mariana.restms.inventory.entity.InventoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, UUID> {

	boolean existsByClinicIdAndName(UUID clinicId, String name);

	boolean existsByClinicIdAndNameAndIdNot(UUID clinicId, String name, UUID id);

	List<InventoryItemEntity> findAllByClinicIdAndActiveTrueOrderByNameAsc(UUID clinicId);
}
