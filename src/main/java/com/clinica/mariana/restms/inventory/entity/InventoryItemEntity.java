package com.clinica.mariana.restms.inventory.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Setter
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

}
