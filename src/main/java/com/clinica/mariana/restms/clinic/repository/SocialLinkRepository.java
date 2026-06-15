package com.clinica.mariana.restms.clinic.repository;

import com.clinica.mariana.restms.clinic.entity.SocialLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SocialLinkRepository extends JpaRepository<SocialLinkEntity, UUID> {

	List<SocialLinkEntity> findAllByClinicIdOrderByCreatedAtAsc(UUID clinicId);

	void deleteAllByClinicId(UUID clinicId);

	boolean existsByClinicIdAndPlatformId(UUID clinicId, UUID platformId);

	boolean existsByClinicIdAndPlatformIdAndIdNot(UUID clinicId, UUID platformId, UUID id);
}
