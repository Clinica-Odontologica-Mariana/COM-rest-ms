package com.clinica.mariana.restms.clinic.entity;

import com.clinica.mariana.restms.address.entity.AddressEntity;
import jakarta.persistence.FetchType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
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

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "address_id", insertable = false, updatable = false)
	private AddressEntity address;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "clinic_id", referencedColumnName = "id", insertable = false, updatable = false)
	@OrderBy("dayOfWeek ASC, startTime ASC")
	private List<WorkingHoursEntity> workingHours;

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
