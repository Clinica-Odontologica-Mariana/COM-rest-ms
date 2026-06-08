package com.clinica.mariana.restms.appointment.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Setter
@Entity
@Table(name = "appointment_status")
public class AppointmentStatusEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "code", nullable = false, unique = true, length = 30)
	private String code;

	@Column(name = "name", nullable = false, unique = true, length = 50)
	private String name;

	@Column(name = "blocks_schedule", nullable = false)
	private boolean blocksSchedule = true;

	@Column(name = "final_status", nullable = false)
	private boolean finalStatus = false;

}
