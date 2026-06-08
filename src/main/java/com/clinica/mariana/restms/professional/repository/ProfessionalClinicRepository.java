package com.clinica.mariana.restms.professional.repository;

import com.clinica.mariana.restms.professional.entity.ProfessionalClinicEntity;
import com.clinica.mariana.restms.professional.entity.ProfessionalClinicId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalClinicRepository extends JpaRepository<ProfessionalClinicEntity, ProfessionalClinicId> {

	List<ProfessionalClinicEntity> findAllByIdProfessionalIdAndActiveTrueOrderByPrimaryClinicDescCreatedAtAsc(
			UUID professionalId);

	Optional<ProfessionalClinicEntity> findFirstByIdProfessionalIdAndActiveTrueOrderByPrimaryClinicDescCreatedAtAsc(
			UUID professionalId);

	@Modifying
	@Query("""
			update ProfessionalClinicEntity membership
			   set membership.primaryClinic = false
			 where membership.id.professionalId = :professionalId
			""")
	void clearPrimaryClinic(@Param("professionalId") UUID professionalId);
}
