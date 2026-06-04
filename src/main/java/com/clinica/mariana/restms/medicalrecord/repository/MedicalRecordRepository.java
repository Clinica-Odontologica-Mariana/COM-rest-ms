package com.clinica.mariana.restms.medicalrecord.repository;

import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecordEntity, UUID> {

	boolean existsByPatientId(UUID patientId);

	Page<MedicalRecordEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

	Optional<MedicalRecordEntity> findByPatientId(UUID patientId);
}
