package com.clinica.mariana.restms.clinic.repository;

import com.clinica.mariana.restms.clinic.entity.WorkingHoursEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface WorkingHoursRepository extends JpaRepository<WorkingHoursEntity, UUID> {

	List<WorkingHoursEntity> findAllByClinicIdOrderByDayOfWeekAscStartTimeAsc(UUID clinicId);

	@Query("""
			SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END
			FROM WorkingHoursEntity w
			WHERE w.clinicId = :clinicId
			  AND w.dayOfWeek = :dayOfWeek
			  AND w.startTime < :endTime
			  AND w.endTime   > :startTime
			""")
	boolean existsOverlap(@Param("clinicId") UUID clinicId, @Param("dayOfWeek") int dayOfWeek,
			@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

	@Query("""
			SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END
			FROM WorkingHoursEntity w
			WHERE w.clinicId = :clinicId
			  AND w.dayOfWeek = :dayOfWeek
			  AND w.id <> :excludeId
			  AND w.startTime < :endTime
			  AND w.endTime   > :startTime
			""")
	boolean existsOverlapExcluding(@Param("clinicId") UUID clinicId, @Param("dayOfWeek") int dayOfWeek,
			@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime,
			@Param("excludeId") UUID excludeId);
}
