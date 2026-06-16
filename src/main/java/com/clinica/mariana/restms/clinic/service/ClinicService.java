package com.clinica.mariana.restms.clinic.service;

import com.clinica.mariana.restms.address.dto.AddressCreateDto;
import com.clinica.mariana.restms.address.dto.AddressDto;
import com.clinica.mariana.restms.clinic.dto.ClinicCreateDto;
import com.clinica.mariana.restms.clinic.dto.ClinicDto;
import com.clinica.mariana.restms.clinic.dto.ClinicUpdateDto;
import com.clinica.mariana.restms.clinic.dto.ClinicWorkingHoursSaveDto;
import com.clinica.mariana.restms.clinic.dto.WorkingHoursDto;
import com.clinica.mariana.restms.clinic.entity.ClinicEntity;
import com.clinica.mariana.restms.clinic.model.ClinicStoredWorkingHours;
import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;
import com.clinica.mariana.restms.storedfile.entity.StoredFileEntity;
import com.clinica.mariana.restms.storedfile.model.FileCategory;
import com.clinica.mariana.restms.storedfile.service.StoredFileService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClinicService {

	private static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";
	private static final String CLINIC_NOT_FOUND = "Clinic not found";

	private final ClinicRepository clinicRepository;
	private final ProfessionalRepository professionalRepository;
	private final StoredFileService storedFileService;
	private final ClinicWorkingHoursJsonSupport workingHoursJsonSupport;

	public ClinicService(ClinicRepository clinicRepository, ProfessionalRepository professionalRepository,
			StoredFileService storedFileService, ClinicWorkingHoursJsonSupport workingHoursJsonSupport) {
		this.clinicRepository = clinicRepository;
		this.professionalRepository = professionalRepository;
		this.storedFileService = storedFileService;
		this.workingHoursJsonSupport = workingHoursJsonSupport;
	}

	@Transactional
	public ClinicDto create(ClinicCreateDto request) {
		return create(request, null);
	}

	@Transactional
	public ClinicDto create(ClinicCreateDto request, MultipartFile photo) {
		ClinicEntity entity = new ClinicEntity();
		apply(entity, request.name(), request.phone(), request.email(), request.timezone(), request.whatsapp(),
				request.instagram(), request.inactiveType(), request.inactiveFrom(), request.inactiveTo(),
				request.address(), request.workingHours());
		entity.setActive(true);
		entity.setInactivatedAt(null);
		ClinicEntity savedEntity = clinicRepository.save(entity);
		if (photo != null && !photo.isEmpty()) {
			savedEntity = replaceClinicPhoto(savedEntity, photo);
		}
		return toDto(savedEntity);
	}

	@Transactional(readOnly = true)
	public Page<ClinicDto> findAll(Pageable pageable) {
		return clinicRepository.findAll(pageable).map(this::toDto);
	}

	@Transactional(readOnly = true)
	public ClinicDto findById(UUID id) {
		return toDto(findEntity(id));
	}

	@Transactional
	public ClinicDto update(UUID id, ClinicUpdateDto request) {
		return update(id, request, null);
	}

	@Transactional
	public ClinicDto update(UUID id, ClinicUpdateDto request, MultipartFile photo) {
		ClinicEntity entity = findEntity(id);
		apply(entity, request.name(), request.phone(), request.email(), request.timezone(), request.whatsapp(),
				request.instagram(), request.inactiveType(), request.inactiveFrom(), request.inactiveTo(),
				request.address(), request.workingHours());
		ClinicEntity savedEntity = clinicRepository.save(entity);
		if (photo != null && !photo.isEmpty()) {
			savedEntity = replaceClinicPhoto(savedEntity, photo);
		}
		return toDto(savedEntity);
	}

	@Transactional
	public ClinicDto uploadPhoto(UUID id, MultipartFile file) {
		return toDto(replaceClinicPhoto(findEntity(id), file));
	}

	@Transactional
	public ClinicDto deletePhoto(UUID id) {
		ClinicEntity entity = findEntity(id);
		UUID oldPhotoFileId = entity.getClinicPhotoFileId();

		if (oldPhotoFileId == null) {
			return toDto(entity);
		}

		entity.setClinicPhotoFileId(null);
		ClinicEntity savedEntity = clinicRepository.saveAndFlush(entity);
		StoredFileEntity oldFile = storedFileService.findActiveByIdAndCategory(oldPhotoFileId,
				FileCategory.CLINIC_PHOTO);
		storedFileService.hardDelete(oldFile);
		return toDto(savedEntity);
	}

	@Transactional
	public ClinicDto inactivate(UUID id) {
		ClinicEntity entity = findEntity(id);
		entity.setActive(false);
		entity.setInactiveType(null);
		entity.setInactiveFrom(null);
		entity.setInactiveTo(null);
		entity.setInactivatedAt(OffsetDateTime.now());
		return toDto(clinicRepository.save(entity));
	}

	@Transactional
	public ClinicDto activate(UUID id) {
		ClinicEntity entity = findEntity(id);
		entity.setActive(true);
		entity.setInactiveType(null);
		entity.setInactiveFrom(null);
		entity.setInactiveTo(null);
		entity.setInactivatedAt(null);
		return toDto(clinicRepository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		ClinicEntity entity = findEntity(id);
		validateClinicDeletion(id);
		UUID photoFileId = entity.getClinicPhotoFileId();

		try {
			clinicRepository.delete(entity);
			clinicRepository.flush();
		} catch (DataIntegrityViolationException ex) {
			throw new AppException(HttpStatus.CONFLICT, "CLINIC_DELETE_BLOCKED",
					"Não foi possível excluir a clínica porque ela possui vínculos com outros registros.");
		}

		if (photoFileId != null) {
			StoredFileEntity photoFile = storedFileService.findActiveByIdAndCategory(photoFileId,
					FileCategory.CLINIC_PHOTO);
			storedFileService.hardDelete(photoFile);
		}
	}

	private void validateClinicDeletion(UUID clinicId) {
		if (professionalRepository.existsByClinicId(clinicId)) {
			throw new AppException(HttpStatus.CONFLICT, "CLINIC_DELETE_BLOCKED",
					"Não foi possível excluir a clínica porque há profissionais vinculados a ela.");
		}
	}

	private ClinicEntity findEntity(UUID id) {
		return clinicRepository.findById(id)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "CLINIC_NOT_FOUND", CLINIC_NOT_FOUND));
	}

	private ClinicEntity replaceClinicPhoto(ClinicEntity entity, MultipartFile file) {
		UUID oldPhotoFileId = entity.getClinicPhotoFileId();
		StoredFileEntity storedFile = storedFileService.upload(file, FileCategory.CLINIC_PHOTO, entity.getId(), null,
				"Clinic photo");
		entity.setClinicPhotoFileId(storedFile.getId());
		ClinicEntity savedEntity = clinicRepository.saveAndFlush(entity);

		if (oldPhotoFileId != null) {
			StoredFileEntity oldFile = storedFileService.findActiveByIdAndCategory(oldPhotoFileId,
					FileCategory.CLINIC_PHOTO);
			storedFileService.hardDelete(oldFile);
		}

		return savedEntity;
	}

	private void apply(ClinicEntity entity, String name, String phone, String email, String timezone, String whatsapp,
			String instagram, String inactiveType, LocalDate inactiveFrom, LocalDate inactiveTo, AddressCreateDto address,
			List<ClinicWorkingHoursSaveDto> workingHours) {
		entity.setName(name);
		entity.setPhone(phone);
		entity.setEmail(email);
		entity.setTimezone(timezone == null || timezone.isBlank() ? DEFAULT_TIMEZONE : timezone);
		entity.setWhatsapp(whatsapp);
		entity.setInstagram(instagram);
		entity.setInactiveType(inactiveType);
		entity.setInactiveFrom(inactiveFrom);
		entity.setInactiveTo(inactiveTo);
		applyAddress(entity, address);
		entity.setWorkingHoursJson(writeWorkingHours(workingHours));
	}

	private void applyAddress(ClinicEntity entity, AddressCreateDto address) {
		if (address == null) {
			entity.setStreet(null);
			entity.setNumber(null);
			entity.setComplement(null);
			entity.setNeighborhood(null);
			entity.setCity(null);
			entity.setState(null);
			entity.setZipCode(null);
			return;
		}

		entity.setStreet(address.street());
		entity.setNumber(address.number());
		entity.setComplement(address.complement());
		entity.setNeighborhood(address.neighborhood());
		entity.setCity(address.city());
		entity.setState(address.state());
		entity.setZipCode(address.zipCode());
	}

	private String writeWorkingHours(List<ClinicWorkingHoursSaveDto> workingHours) {
		List<ClinicStoredWorkingHours> normalized = workingHoursJsonSupport.fromSaveDtos(workingHours);
		return workingHoursJsonSupport.write(normalized);
	}

	private ClinicDto toDto(ClinicEntity entity) {
		String clinicPhotoUrl = entity.getClinicPhotoFileId() == null
				? null
				: storedFileService.presignedDownloadUrl(entity.getClinicPhotoFileId(), FileCategory.CLINIC_PHOTO)
						.url();
		AddressDto address = toAddressDto(entity);
		List<WorkingHoursDto> workingHours = workingHoursJsonSupport.toDtos(entity.getId(), entity.getWorkingHoursJson());
		return new ClinicDto(entity.getId(), null, entity.getName(), entity.getPhone(), entity.getEmail(),
				entity.getTimezone(), entity.getWhatsapp(), entity.getInstagram(), entity.getClinicPhotoFileId(),
				clinicPhotoUrl, entity.getInactiveType(), entity.getInactiveFrom(), entity.getInactiveTo(),
				entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt(), address, workingHours);
	}

	private AddressDto toAddressDto(ClinicEntity entity) {
		if (entity.getStreet() == null && entity.getNumber() == null && entity.getNeighborhood() == null
				&& entity.getCity() == null && entity.getState() == null && entity.getZipCode() == null
				&& entity.getComplement() == null) {
			return null;
		}

		return new AddressDto(null, entity.getStreet(), entity.getNumber(), entity.getComplement(),
				entity.getNeighborhood(), entity.getCity(), entity.getState(), entity.getZipCode());
	}
}
