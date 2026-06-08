package com.clinica.mariana.restms.patient.repository;

import com.clinica.mariana.restms.patient.entity.PatientClinicEntity;
import com.clinica.mariana.restms.patient.entity.PatientClinicId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientClinicRepository extends JpaRepository<PatientClinicEntity, PatientClinicId> {

	List<PatientClinicEntity> findAllByIdPatientIdAndActiveTrueOrderByPrimaryClinicDescCreatedAtAsc(UUID patientId);

	Optional<PatientClinicEntity> findFirstByIdPatientIdAndActiveTrueOrderByPrimaryClinicDescCreatedAtAsc(
			UUID patientId);

	@Modifying
	@Query("""
			update PatientClinicEntity membership
			   set membership.primaryClinic = false
			 where membership.id.patientId = :patientId
			""")
	void clearPrimaryClinic(@Param("patientId") UUID patientId);
}
