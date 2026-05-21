package com.clinica.mariana.restms.medicalrecord.repository;

import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecordEntity, UUID> {

	Optional<MedicalRecordEntity> findByPatientId(UUID patientId);

	boolean existsByPatientId(UUID patientId);
}
