package com.clinica.mariana.restms.medicalrecord.repository;

import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecordEntity, UUID> {

	boolean existsByPatientId(UUID patientId);

	List<MedicalRecordEntity> findAllByOrderByUpdatedAtDesc();

	Optional<MedicalRecordEntity> findByPatientId(UUID patientId);
}
