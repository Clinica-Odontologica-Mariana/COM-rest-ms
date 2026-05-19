package com.clinica.mariana.restms.clinic.repository;

import com.clinica.mariana.restms.clinic.entity.SocialPlatformEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SocialPlataformRepository extends JpaRepository<SocialPlatformEntity, UUID> {

    List<SocialPlatformEntity> findAllByOrderByNameAsc();
}