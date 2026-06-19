package com.clinica.mariana.restms.patient.repository;

import com.clinica.mariana.restms.patient.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<PatientEntity, UUID> {

	boolean existsByCpf(String cpf);

	boolean existsByCpfAndIdNot(String cpf, UUID id);

	Optional<PatientEntity> findByCpf(String cpf);

	Page<PatientEntity> findAllByActiveTrueOrderByFullNameAsc(Pageable pageable);

	Page<PatientEntity> findAllByOrderByFullNameAsc(Pageable pageable);

	void deleteById(UUID id);
}
