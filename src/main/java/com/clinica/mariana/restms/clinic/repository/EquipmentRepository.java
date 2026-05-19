package com.clinica.mariana.restms.clinic.repository;

import com.clinica.mariana.restms.clinic.entity.EquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EquipmentRepository extends JpaRepository<EquipmentEntity, UUID> {

    List<EquipmentEntity> findAllByClinicIdAndActiveTrueOrderByNameAsc(UUID clinicId);

    List<EquipmentEntity> findAllByClinicIdOrderByNameAsc(UUID clinicId);
}