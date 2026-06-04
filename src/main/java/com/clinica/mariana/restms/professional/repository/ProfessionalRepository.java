package com.clinica.mariana.restms.professional.repository;

import com.clinica.mariana.restms.professional.entity.ProfessionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface ProfessionalRepository extends JpaRepository<ProfessionalEntity, UUID> {

	boolean existsByUserId(UUID userId);

	boolean existsByUserIdAndIdNot(UUID userId, UUID id);

	boolean existsByClinicIdAndLicenseNumber(UUID clinicId, String licenseNumber);

	boolean existsByClinicIdAndLicenseNumberAndIdNot(UUID clinicId, String licenseNumber, UUID id);

	Page<ProfessionalEntity> findAllByActiveTrueOrderByLicenseNumberAsc(Pageable pageable);
}
