package com.clinica.mariana.restms.clinic.repository;

import com.clinica.mariana.restms.clinic.entity.WorkingHoursEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkingHoursRepository extends JpaRepository<WorkingHoursEntity, UUID> {

    List<WorkingHoursEntity> findAllByClinicIdOrderByDayOfWeekAscStartTimeAsc(UUID clinicId);

    boolean existsByClinicIdAndDayOfWeek(UUID clinicId, int dayOfWeek);

    boolean existsByClinicIdAndDayOfWeekAndIdNot(UUID clinicId, int dayOfWeek, UUID id);
}