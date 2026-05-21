package com.clinica.mariana.restms.storedfile.repository;

import com.clinica.mariana.restms.storedfile.entity.StoredFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFileEntity, UUID> {
}
