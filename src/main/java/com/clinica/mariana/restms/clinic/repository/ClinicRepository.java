package com.clinica.mariana.restms.clinic.repository;

import com.clinica.mariana.restms.clinic.entity.ClinicEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClinicRepository extends JpaRepository<ClinicEntity, UUID> {

	boolean existsByDocument(String document);

	boolean existsByDocumentAndIdNot(String document, UUID id);

	Page<ClinicEntity> findAllByActiveTrueOrderByNameAsc(Pageable pageable);

	Optional<ClinicEntity> findByDocument(String document);
}
