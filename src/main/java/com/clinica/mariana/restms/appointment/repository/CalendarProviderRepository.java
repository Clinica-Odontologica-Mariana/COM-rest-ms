package com.clinica.mariana.restms.appointment.repository;

import com.clinica.mariana.restms.appointment.entity.CalendarProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CalendarProviderRepository extends JpaRepository<CalendarProviderEntity, UUID> {

	Optional<CalendarProviderEntity> findByCode(String code);
}
