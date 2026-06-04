package com.clinica.mariana.restms.inventory.entity;

import lombok.Getter;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedBy;
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

@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@Setter
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

	@CreatedBy
	@Column(name = "created_by_user_id", updatable = false)
	private UUID createdByUserId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

}
