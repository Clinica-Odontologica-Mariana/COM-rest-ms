package com.clinica.mariana.restms.patient.repository;

import com.clinica.mariana.restms.patient.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<PatientEntity, UUID> {

	boolean existsByCpf(String cpf);

	boolean existsByCpfAndIdNot(String cpf, UUID id);

	List<PatientEntity> findAllByActiveTrueOrderByFullNameAsc();

	Optional<PatientEntity> findByCpf(String cpf);
}
