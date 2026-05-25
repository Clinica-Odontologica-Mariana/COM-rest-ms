package com.clinica.mariana.restms.storedfile.service;

import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordEntity;
import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordRepository;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import com.clinica.mariana.restms.storedfile.dto.OdontogramFileDto;
import com.clinica.mariana.restms.storedfile.dto.PresignedUrlDto;
import com.clinica.mariana.restms.storedfile.entity.OdontogramFileEntity;
import com.clinica.mariana.restms.storedfile.entity.StoredFileEntity;
import com.clinica.mariana.restms.storedfile.model.FileCategory;
import com.clinica.mariana.restms.storedfile.repository.OdontogramFileRepository;
import com.clinica.mariana.restms.storedfile.repository.StoredFileReferenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class OdontogramFileService {

	private final OdontogramFileRepository odontogramFileRepository;
	private final StoredFileService storedFileService;
	private final PatientRepository patientRepository;
	private final MedicalRecordRepository medicalRecordRepository;
	private final StoredFileReferenceRepository referenceRepository;

	public OdontogramFileService(OdontogramFileRepository odontogramFileRepository, StoredFileService storedFileService,
			PatientRepository patientRepository, MedicalRecordRepository medicalRecordRepository,
			StoredFileReferenceRepository referenceRepository) {
		this.odontogramFileRepository = odontogramFileRepository;
		this.storedFileService = storedFileService;
		this.patientRepository = patientRepository;
		this.medicalRecordRepository = medicalRecordRepository;
		this.referenceRepository = referenceRepository;
	}

	@Transactional
	public OdontogramFileDto upload(UUID patientId, UUID medicalRecordId, UUID odontogramEntryId, String description,
			MultipartFile file, UUID uploadedByUserId) {
		if (!patientRepository.existsById(patientId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", "Patient not found");
		}

		UUID resolvedMedicalRecordId = resolveMedicalRecordId(patientId, medicalRecordId);
		if (odontogramEntryId != null && !referenceRepository.odontogramEntryExists(odontogramEntryId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "ODONTOGRAM_ENTRY_NOT_FOUND", "Odontogram entry not found");
		}

		StoredFileEntity storedFile = storedFileService.upload(file, FileCategory.ODONTOGRAM, patientId,
				uploadedByUserId, description);

		try {
			OdontogramFileEntity entity = new OdontogramFileEntity();
			entity.setPatientId(patientId);
			entity.setMedicalRecordId(resolvedMedicalRecordId);
			entity.setOdontogramEntryId(odontogramEntryId);
			entity.setStoredFileId(storedFile.getId());
			entity.setDescription(description);
			entity.setCreatedByUserId(uploadedByUserId);
			return toDto(odontogramFileRepository.save(entity), storedFile);
		} catch (RuntimeException ex) {
			storedFileService.hardDelete(storedFile);
			throw ex;
		}
	}

	@Transactional(readOnly = true)
	public OdontogramFileDto findById(UUID id) {
		OdontogramFileEntity link = findLink(id);
		StoredFileEntity file = storedFileService.findActiveByIdAndCategory(link.getStoredFileId(),
				FileCategory.ODONTOGRAM);
		return toDto(link, file);
	}

	@Transactional(readOnly = true)
	public List<OdontogramFileDto> findByPatient(UUID patientId) {
		if (!patientRepository.existsById(patientId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", "Patient not found");
		}
		return odontogramFileRepository.findAllByPatientIdOrderByCreatedAtDesc(patientId).stream().map(link -> {
			StoredFileEntity file = storedFileService.findActiveByIdAndCategory(link.getStoredFileId(),
					FileCategory.ODONTOGRAM);
			return toDto(link, file);
		}).toList();
	}

	@Transactional(readOnly = true)
	public PresignedUrlDto presignedDownloadUrl(UUID id) {
		OdontogramFileEntity link = findLink(id);
		return storedFileService.presignedDownloadUrl(link.getStoredFileId(), FileCategory.ODONTOGRAM);
	}

	@Transactional
	public void delete(UUID id) {
		OdontogramFileEntity link = findLink(id);
		StoredFileEntity file = storedFileService.findActiveByIdAndCategory(link.getStoredFileId(),
				FileCategory.ODONTOGRAM);
		odontogramFileRepository.delete(link);
		storedFileService.hardDelete(file);
	}

	private UUID resolveMedicalRecordId(UUID patientId, UUID medicalRecordId) {
		if (medicalRecordId != null) {
			if (!referenceRepository.medicalRecordExists(medicalRecordId)) {
				throw new AppException(HttpStatus.NOT_FOUND, "MEDICAL_RECORD_NOT_FOUND", "Medical record not found");
			}
			return medicalRecordId;
		}
		return medicalRecordRepository.findByPatientId(patientId).map(MedicalRecordEntity::getId).orElse(null);
	}

	private OdontogramFileEntity findLink(UUID id) {
		return odontogramFileRepository.findById(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "ODONTOGRAM_FILE_NOT_FOUND", "Odontogram file not found"));
	}

	private OdontogramFileDto toDto(OdontogramFileEntity link, StoredFileEntity file) {
		return new OdontogramFileDto(link.getId(), link.getPatientId(), link.getMedicalRecordId(),
				link.getOdontogramEntryId(), storedFileService.toDto(file), link.getDescription(), link.getCreatedAt());
	}
}
