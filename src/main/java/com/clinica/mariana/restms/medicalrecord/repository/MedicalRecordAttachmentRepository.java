package com.clinica.mariana.restms.medicalrecord.repository;

import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordAttachmentRepository extends JpaRepository<MedicalRecordAttachmentEntity, UUID> {

	List<MedicalRecordAttachmentEntity> findAllByMedicalRecordIdOrderByCreatedAtDesc(UUID medicalRecordId);

	boolean existsByStoredFileId(UUID storedFileId);

	Optional<MedicalRecordAttachmentEntity> findByIdAndMedicalRecordId(UUID id, UUID medicalRecordId);
}
