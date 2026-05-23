package com.clinica.mariana.restms.workplace.repository;

import com.clinica.mariana.restms.workplace.entity.WorkplaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkplaceRepository extends JpaRepository<WorkplaceEntity, UUID> {

	boolean existsByClinicIdAndName(UUID clinicId, String name);

	boolean existsByClinicIdAndNameAndIdNot(UUID clinicId, String name, UUID id);

	List<WorkplaceEntity> findAllByClinicIdAndActiveTrueOrderByNameAsc(UUID clinicId);

	Optional<WorkplaceEntity> findByClinicIdAndName(UUID clinicId, String name);

	List<WorkplaceEntity> findAllByClinicIdOrderByNameAsc(UUID clinicId);
}
