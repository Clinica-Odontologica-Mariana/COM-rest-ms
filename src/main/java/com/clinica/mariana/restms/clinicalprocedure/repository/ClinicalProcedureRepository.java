package com.clinica.mariana.restms.clinicalprocedure.repository;

import com.clinica.mariana.restms.clinicalprocedure.entity.ClinicalProcedureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClinicalProcedureRepository extends JpaRepository<ClinicalProcedureEntity, UUID> {

	boolean existsByCode(String code);

	boolean existsByCodeAndIdNot(String code, UUID id);

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, UUID id);

	Page<ClinicalProcedureEntity> findAllByActiveTrueOrderByNameAsc(Pageable pageable);
}
