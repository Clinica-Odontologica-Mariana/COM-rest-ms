package com.clinica.mariana.restms.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_cost")
public class ServiceCostEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "service_id", nullable = false)
	private UUID serviceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cost_type_id", nullable = false)
	private ServiceCostTypeEntity costType;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "description")
	private String description;

	@Column(name = "valid_from", nullable = false, updatable = false)
	private LocalDate validFrom;

	@Column(name = "valid_to")
	private LocalDate validTo;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getServiceId() {
		return serviceId;
	}

	public void setServiceId(UUID serviceId) {
		this.serviceId = serviceId;
	}

	public ServiceCostTypeEntity getCostType() {
		return costType;
	}

	public void setCostType(ServiceCostTypeEntity costType) {
		this.costType = costType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
