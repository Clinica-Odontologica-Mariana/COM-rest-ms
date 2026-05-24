package com.clinica.mariana.restms.storedfile.repository;

import com.clinica.mariana.restms.storedfile.entity.StoredFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import com.clinica.mariana.restms.storedfile.model.FileCategory;

import java.util.Optional;
import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFileEntity, UUID> {

	Optional<StoredFileEntity> findByIdAndFileCategory(UUID id, FileCategory fileCategory);
}
