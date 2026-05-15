package com.clinica.mariana.restms.appointment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

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

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isBlocksSchedule() {
		return blocksSchedule;
	}

	public void setBlocksSchedule(boolean blocksSchedule) {
		this.blocksSchedule = blocksSchedule;
	}

	public boolean isFinalStatus() {
		return finalStatus;
	}

	public void setFinalStatus(boolean finalStatus) {
		this.finalStatus = finalStatus;
	}
}
