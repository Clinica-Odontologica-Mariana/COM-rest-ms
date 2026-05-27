package com.clinica.mariana.restms.patient.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PatientClinicId implements Serializable {

	@Column(name = "patient_id", nullable = false)
	private UUID patientId;

	@Column(name = "clinic_id", nullable = false)
	private UUID clinicId;

	public PatientClinicId() {
	}

	public PatientClinicId(UUID patientId, UUID clinicId) {
		this.patientId = patientId;
		this.clinicId = clinicId;
	}

	public UUID getPatientId() {
		return patientId;
	}

	public void setPatientId(UUID patientId) {
		this.patientId = patientId;
	}

	public UUID getClinicId() {
		return clinicId;
	}

	public void setClinicId(UUID clinicId) {
		this.clinicId = clinicId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PatientClinicId that)) {
			return false;
		}
		return Objects.equals(patientId, that.patientId) && Objects.equals(clinicId, that.clinicId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(patientId, clinicId);
	}
}
