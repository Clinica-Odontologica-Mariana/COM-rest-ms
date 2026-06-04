package com.clinica.mariana.restms.clinic.repository;

import com.clinica.mariana.restms.clinic.entity.SocialPlatformEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface SocialPlatformRepository extends JpaRepository<SocialPlatformEntity, UUID> {

	List<SocialPlatformEntity> findAllByOrderByNameAsc();

	Optional<SocialPlatformEntity> findByCode(String code);

}
