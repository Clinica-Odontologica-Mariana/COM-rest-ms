package com.clinica.mariana.restms.clinic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
@Entity
@Table(name = "clinic")
public class ClinicEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "phone", nullable = false, length = 20)
	private String phone;

	@Column(name = "email", length = 150)
	private String email;

	@Column(name = "timezone", nullable = false, length = 80)
	private String timezone = "America/Sao_Paulo";

	@Column(name = "whatsapp", length = 20)
	private String whatsapp;

	@Column(name = "instagram", length = 80)
	private String instagram;

	@Column(name = "street", length = 150)
	private String street;

	@Column(name = "number", length = 20)
	private String number;

	@Column(name = "complement", length = 100)
	private String complement;

	@Column(name = "neighborhood", length = 100)
	private String neighborhood;

	@Column(name = "city", length = 100)
	private String city;

	@Column(name = "state", length = 2)
	private String state;

	@Column(name = "zip_code", length = 8)
	private String zipCode;

	@Column(name = "working_hours_json", nullable = false)
	private String workingHoursJson = "[]";

	@Column(name = "clinic_photo_file_id")
	private UUID clinicPhotoFileId;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "inactive_type", length = 20)
	private String inactiveType;

	@Column(name = "inactive_from")
	private LocalDate inactiveFrom;

	@Column(name = "inactive_to")
	private LocalDate inactiveTo;

	@Column(name = "inactivated_at")
	private OffsetDateTime inactivatedAt;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

}
