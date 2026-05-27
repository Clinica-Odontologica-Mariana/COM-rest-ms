package com.clinica.mariana.restms.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movement")
public class StockMovementEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "inventory_item_id", nullable = false)
	private UUID inventoryItemId;

	@Column(name = "movement_type", nullable = false, length = 20)
	private String movementType;

	@Column(name = "quantity", nullable = false)
	private BigDecimal quantity;

	@Column(name = "reason", length = 255)
	private String reason;

	@Column(name = "created_by_user_id")
	private UUID createdByUserId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	public UUID getId() {
		return id;
	}

	public UUID getInventoryItemId() {
		return inventoryItemId;
	}

	public void setInventoryItemId(UUID inventoryItemId) {
		this.inventoryItemId = inventoryItemId;
	}

	public String getMovementType() {
		return movementType;
	}

	public void setMovementType(String movementType) {
		this.movementType = movementType;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public UUID getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(UUID createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
