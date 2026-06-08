package com.clinica.mariana.restms.professional.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Setter
@Embeddable
public class ProfessionalClinicId implements Serializable {

	@Column(name = "professional_id", nullable = false)
	private UUID professionalId;

	@Column(name = "clinic_id", nullable = false)
	private UUID clinicId;

	public ProfessionalClinicId(UUID professionalId, UUID clinicId) {
		this.professionalId = professionalId;
		this.clinicId = clinicId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ProfessionalClinicId that)) {
			return false;
		}
		return Objects.equals(professionalId, that.professionalId) && Objects.equals(clinicId, that.clinicId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(professionalId, clinicId);
	}
}
