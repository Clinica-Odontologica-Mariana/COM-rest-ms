package com.clinica.mariana.restms.service.repository;

import com.clinica.mariana.restms.service.entity.ServicePriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServicePriceRepository extends JpaRepository<ServicePriceEntity, UUID> {

	List<ServicePriceEntity> findAllByServiceIdOrderByValidFromDesc(UUID serviceId);
}
