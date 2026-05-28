package com.clinica.mariana.restms.service.repository;

import com.clinica.mariana.restms.service.entity.ServiceCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategoryEntity, UUID> {

	List<ServiceCategoryEntity> findAllByOrderByNameAsc();

	Optional<ServiceCategoryEntity> findByCode(String code);
}
