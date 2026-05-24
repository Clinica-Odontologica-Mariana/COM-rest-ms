package com.clinica.mariana.restms.appointment.repository;

import com.clinica.mariana.restms.appointment.entity.AppointmentStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppointmentStatusRepository extends JpaRepository<AppointmentStatusEntity, UUID> {

	Optional<AppointmentStatusEntity> findByCode(String code);
}
