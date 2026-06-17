package com.clinica.mariana.restms.clinic.service;

import com.clinica.mariana.restms.clinic.dto.ClinicWorkingHoursSaveDto;
import com.clinica.mariana.restms.clinic.dto.WorkingHoursDto;
import com.clinica.mariana.restms.clinic.model.ClinicStoredWorkingHours;
import com.clinica.mariana.restms.common.exception.AppException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ClinicWorkingHoursJsonSupport {

	private static final TypeReference<List<ClinicStoredWorkingHours>> TYPE = new TypeReference<>() {
	};
	private static final Comparator<ClinicStoredWorkingHours> SORT = Comparator
			.comparingInt(ClinicStoredWorkingHours::dayOfWeek).thenComparing(ClinicStoredWorkingHours::startTime);

	private final ObjectMapper objectMapper;

	public ClinicWorkingHoursJsonSupport(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public List<ClinicStoredWorkingHours> read(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}

		try {
			return objectMapper.readValue(json, TYPE).stream().sorted(SORT).toList();
		} catch (JsonProcessingException ex) {
			throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "CLINIC_WORKING_HOURS_INVALID",
					"Stored clinic working hours are invalid");
		}
	}

	public String write(List<ClinicStoredWorkingHours> workingHours) {
		try {
			return objectMapper
					.writeValueAsString(workingHours == null ? List.of() : workingHours.stream().sorted(SORT).toList());
		} catch (JsonProcessingException ex) {
			throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "CLINIC_WORKING_HOURS_SERIALIZATION_FAILED",
					"Failed to serialize clinic working hours");
		}
	}

	public List<ClinicStoredWorkingHours> fromSaveDtos(List<ClinicWorkingHoursSaveDto> workingHours) {
		if (workingHours == null || workingHours.isEmpty()) {
			return List.of();
		}

		List<ClinicStoredWorkingHours> stored = workingHours.stream()
				.map(hours -> new ClinicStoredWorkingHours(UUID.randomUUID(), hours.dayOfWeek(), hours.startTime(),
						hours.endTime()))
				.sorted(SORT).toList();
		validateNoOverlap(stored);
		return stored;
	}

	public List<WorkingHoursDto> toDtos(UUID clinicId, String json) {
		return toDtos(clinicId, read(json));
	}

	public List<WorkingHoursDto> toDtos(UUID clinicId, List<ClinicStoredWorkingHours> workingHours) {
		return workingHours.stream().sorted(SORT).map(hours -> new WorkingHoursDto(hours.id(), clinicId,
				hours.dayOfWeek(), hours.startTime(), hours.endTime())).toList();
	}

	public void validateNoOverlap(List<ClinicStoredWorkingHours> workingHours) {
		List<ClinicStoredWorkingHours> sorted = new ArrayList<>(workingHours.stream().sorted(SORT).toList());

		for (int index = 0; index < sorted.size(); index++) {
			ClinicStoredWorkingHours current = sorted.get(index);
			validateTimeRange(current.startTime(), current.endTime());

			for (int nextIndex = index + 1; nextIndex < sorted.size(); nextIndex++) {
				ClinicStoredWorkingHours candidate = sorted.get(nextIndex);
				if (candidate.dayOfWeek() != current.dayOfWeek()) {
					break;
				}
				if (overlaps(current, candidate)) {
					throw new AppException(HttpStatus.CONFLICT, "WORKING_HOURS_OVERLAP",
							"Working hours overlap with an existing interval for this clinic and day");
				}
			}
		}
	}

	private boolean overlaps(ClinicStoredWorkingHours left, ClinicStoredWorkingHours right) {
		return left.startTime().isBefore(right.endTime()) && left.endTime().isAfter(right.startTime());
	}

	private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
		if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
			throw new IllegalArgumentException("endTime must be after startTime");
		}
	}
}
