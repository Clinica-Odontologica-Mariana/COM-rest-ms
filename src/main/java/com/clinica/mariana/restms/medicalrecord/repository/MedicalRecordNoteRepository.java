package com.clinica.mariana.restms.medicalrecord.repository;

import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordNoteRepository extends JpaRepository<MedicalRecordNoteEntity, UUID> {

	List<MedicalRecordNoteEntity> findAllByMedicalRecordIdOrderByCreatedAtDesc(UUID medicalRecordId);

	Optional<MedicalRecordNoteEntity> findByIdAndMedicalRecordId(UUID id, UUID medicalRecordId);
}
