package com.clinica.mariana.restms.clinic.repository;

import com.clinica.mariana.restms.clinic.entity.ClinicEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClinicRepository extends JpaRepository<ClinicEntity, UUID> {

    boolean existsByDocument(String document);

    boolean existsByDocumentAndIdNot(String document, UUID id);

    Optional<ClinicEntity> findByDocument(String document);

    List<ClinicEntity> findAllByActiveTrueOrderByNameAsc();

}