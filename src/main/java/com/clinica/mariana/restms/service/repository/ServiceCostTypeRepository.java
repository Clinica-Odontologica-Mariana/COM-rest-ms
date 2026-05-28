package com.clinica.mariana.restms.service.repository;

import com.clinica.mariana.restms.service.entity.ServiceCostTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceCostTypeRepository extends JpaRepository<ServiceCostTypeEntity, UUID> {

	Optional<ServiceCostTypeEntity> findByCode(String code);
}
