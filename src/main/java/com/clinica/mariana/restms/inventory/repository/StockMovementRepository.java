package com.clinica.mariana.restms.inventory.repository;

import com.clinica.mariana.restms.inventory.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovementEntity, UUID> {

	List<StockMovementEntity> findAllByInventoryItemIdOrderByCreatedAtDesc(UUID inventoryItemId);
}
