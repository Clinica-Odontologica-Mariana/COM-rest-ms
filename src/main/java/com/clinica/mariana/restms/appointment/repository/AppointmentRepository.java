package com.clinica.mariana.restms.appointment.repository;

import com.clinica.mariana.restms.appointment.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

	Page<AppointmentEntity> findAllByCancelledAtIsNullOrderByStartDatetimeAsc(Pageable pageable);

	Page<AppointmentEntity> findByCancelledAtIsNullAndStartDatetimeBetweenOrderByStartDatetimeAsc(OffsetDateTime start,
			OffsetDateTime end, Pageable pageable);
}
