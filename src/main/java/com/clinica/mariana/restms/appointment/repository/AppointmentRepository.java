package com.clinica.mariana.restms.appointment.repository;

import com.clinica.mariana.restms.appointment.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

	List<AppointmentEntity> findAllByCancelledAtIsNullOrderByStartDatetimeAsc();

	List<AppointmentEntity> findByCancelledAtIsNullAndStartDatetimeBetweenOrderByStartDatetimeAsc(
			OffsetDateTime start, OffsetDateTime end);
}
