package com.clinica.mariana.restms.clinic.repository;

import com.clinica.mariana.restms.clinic.entity.ClinicEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClinicRepository extends JpaRepository<ClinicEntity, UUID> {

	@Override
	@EntityGraph(attributePaths = {"address", "workingHours"})
	Page<ClinicEntity> findAll(Pageable pageable);

	@Override
	@EntityGraph(attributePaths = {"address", "workingHours"})
	Optional<ClinicEntity> findById(UUID id);

	boolean existsByAddressId(UUID addressId);
}
