package com.clinica.mariana.restms.appointment.repository;

import com.clinica.mariana.restms.appointment.entity.CalendarSyncStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CalendarSyncStatusRepository extends JpaRepository<CalendarSyncStatusEntity, UUID> {

	Optional<CalendarSyncStatusEntity> findByCode(String code);
}
