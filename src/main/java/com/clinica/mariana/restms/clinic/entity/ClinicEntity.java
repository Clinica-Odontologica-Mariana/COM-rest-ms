package com.clinica.mariana.restms.clinic.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Setter
@Entity
@Table(name = "clinic")
public class ClinicEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "address_id")
	private UUID addressId;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "document", nullable = false, length = 14, unique = true)
	private String document;

	@Column(name = "phone", nullable = false, length = 20)
	private String phone;

	@Column(name = "email", length = 150)
	private String email;

	@Column(name = "timezone", nullable = false, length = 80)
	private String timezone = "America/Sao_Paulo";

	@Column(name = "description")
	private String description;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "inactivated_at")
	private OffsetDateTime inactivatedAt;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

}
