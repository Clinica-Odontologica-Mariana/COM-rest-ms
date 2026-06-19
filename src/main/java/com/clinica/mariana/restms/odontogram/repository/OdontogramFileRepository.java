package com.clinica.mariana.restms.odontogram.repository;

import com.clinica.mariana.restms.odontogram.entity.OdontogramFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OdontogramFileRepository extends JpaRepository<OdontogramFileEntity, UUID> {

	List<OdontogramFileEntity> findAllByPatientIdOrderByCreatedAtDesc(UUID patientId);

	Optional<OdontogramFileEntity> findById(UUID id);

	void deleteByPatientId(UUID patientId);
}
