package com.clinica.mariana.restms.clinic.repository;

import com.clinica.mariana.restms.clinic.entity.ClinicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ClinicRepository extends JpaRepository<ClinicEntity, UUID> {
}
