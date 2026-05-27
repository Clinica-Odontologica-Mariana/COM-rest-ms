package com.clinica.mariana.restms.odontogram.repository;

import com.clinica.mariana.restms.odontogram.entity.OdontogramEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OdontogramEntryRepository extends JpaRepository<OdontogramEntryEntity, UUID> {

	List<OdontogramEntryEntity> findAllByPatientIdOrderByRecordedAtDesc(UUID patientId);

	List<OdontogramEntryEntity> findAllByMedicalRecordIdOrderByRecordedAtDesc(UUID medicalRecordId);
}
