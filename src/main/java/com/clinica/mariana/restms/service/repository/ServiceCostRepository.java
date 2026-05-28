package com.clinica.mariana.restms.service.repository;

import com.clinica.mariana.restms.service.entity.ServiceCostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceCostRepository extends JpaRepository<ServiceCostEntity, UUID> {

	List<ServiceCostEntity> findAllByServiceIdOrderByValidFromDesc(UUID serviceId);
}
