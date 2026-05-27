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
@Table(name = "inventory_item")
public class InventoryItemEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "clinic_id", nullable = false)
	private UUID clinicId;

	@Column(name = "item_type", nullable = false, length = 30)
	private String itemType;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "sku", length = 80)
	private String sku;

	@Column(name = "unit", length = 30)
	private String unit;

	@Column(name = "current_quantity", nullable = false)
	private BigDecimal currentQuantity = BigDecimal.ZERO;

	@Column(name = "minimum_quantity")
	private BigDecimal minimumQuantity;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

	public UUID getId() {
		return id;
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public void setClinicId(UUID clinicId) {
		this.clinicId = clinicId;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public BigDecimal getCurrentQuantity() {
		return currentQuantity;
	}

	public void setCurrentQuantity(BigDecimal currentQuantity) {
		this.currentQuantity = currentQuantity;
	}

	public BigDecimal getMinimumQuantity() {
		return minimumQuantity;
	}

	public void setMinimumQuantity(BigDecimal minimumQuantity) {
		this.minimumQuantity = minimumQuantity;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
