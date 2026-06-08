package com.clinica.mariana.restms.medicalrecord.service;

import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentUpdateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteUpdateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordUpdateDto;
import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordAttachmentEntity;
import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordEntity;
import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordNoteEntity;
import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordAttachmentRepository;
import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordNoteRepository;
import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordRepository;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import com.clinica.mariana.restms.storedfile.entity.StoredFileEntity;
import com.clinica.mariana.restms.storedfile.repository.StoredFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clinica.mariana.restms.common.exception.AppException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@Service
public class MedicalRecordService {

	private static final String MEDICAL_RECORD_NOT_FOUND = "Medical record not found";

	private final MedicalRecordRepository medicalRecordRepository;
	private final MedicalRecordNoteRepository noteRepository;
	private final MedicalRecordAttachmentRepository attachmentRepository;
	private final PatientRepository patientRepository;
	private final StoredFileRepository storedFileRepository;

	public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
			MedicalRecordNoteRepository noteRepository, MedicalRecordAttachmentRepository attachmentRepository,
			PatientRepository patientRepository, StoredFileRepository storedFileRepository) {
		this.medicalRecordRepository = medicalRecordRepository;
		this.noteRepository = noteRepository;
		this.attachmentRepository = attachmentRepository;
		this.patientRepository = patientRepository;
		this.storedFileRepository = storedFileRepository;
	}

	@Transactional
	public MedicalRecordDto create(MedicalRecordCreateDto request) {
		if (!patientRepository.existsById(request.patientId())) {
			throw new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", "Patient not found");
		}

		if (medicalRecordRepository.existsByPatientId(request.patientId())) {
			throw new AppException(HttpStatus.CONFLICT, "MEDICAL_RECORD_ALREADY_EXISTS",
					"Medical record already exists for patient");
		}

		MedicalRecordEntity entity = new MedicalRecordEntity();
		entity.setPatientId(request.patientId());
		entity.setAllergies(request.allergies());
		entity.setChronicConditions(request.chronicConditions());
		entity.setContinuousMedications(request.continuousMedications());
		entity.setGeneralObservations(request.generalObservations());

		return toDto(medicalRecordRepository.save(entity));
	}

	@Transactional
	public MedicalRecordDto createForPatientIfMissing(UUID patientId) {
		return medicalRecordRepository.findByPatientId(patientId).map(this::toDto).orElseGet(() -> {
			MedicalRecordEntity entity = new MedicalRecordEntity();
			entity.setPatientId(patientId);
			return toDto(medicalRecordRepository.save(entity));
		});
	}

	@Transactional(readOnly = true)
	public Page<MedicalRecordDto> findAll(Pageable pageable) {
		return medicalRecordRepository.findAllByOrderByUpdatedAtDesc(pageable).map(this::toDto);
	}

	@Transactional(readOnly = true)
	public MedicalRecordDto findById(UUID id) {
		MedicalRecordEntity entity = medicalRecordRepository.findById(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "MEDICAL_RECORD_NOT_FOUND", MEDICAL_RECORD_NOT_FOUND));

		return toDto(entity);
	}

	@Transactional(readOnly = true)
	public MedicalRecordDto findByPatientId(UUID patientId) {
		MedicalRecordEntity entity = findEntityByPatientId(patientId);

		return toDto(entity);
	}

	@Transactional
	public MedicalRecordDto update(UUID id, MedicalRecordUpdateDto request) {
		MedicalRecordEntity entity = medicalRecordRepository.findById(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "MEDICAL_RECORD_NOT_FOUND", MEDICAL_RECORD_NOT_FOUND));

		entity.setAllergies(request.allergies());
		entity.setChronicConditions(request.chronicConditions());
		entity.setContinuousMedications(request.continuousMedications());
		entity.setGeneralObservations(request.generalObservations());

		return toDto(medicalRecordRepository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		MedicalRecordEntity entity = medicalRecordRepository.findById(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "MEDICAL_RECORD_NOT_FOUND", MEDICAL_RECORD_NOT_FOUND));

		medicalRecordRepository.delete(entity);
	}

	@Transactional
	public MedicalRecordNoteDto addNote(UUID patientId, MedicalRecordNoteCreateDto request) {
		MedicalRecordEntity medicalRecord = findEntityByPatientId(patientId);

		MedicalRecordNoteEntity note = new MedicalRecordNoteEntity();
		note.setMedicalRecordId(medicalRecord.getId());
		note.setNote(request.note());

		return toDto(noteRepository.save(note));
	}

	@Transactional(readOnly = true)
	public List<MedicalRecordNoteDto> findNotesByPatientId(UUID patientId) {
		MedicalRecordEntity medicalRecord = findEntityByPatientId(patientId);

		return noteRepository.findAllByMedicalRecordIdOrderByCreatedAtDesc(medicalRecord.getId()).stream()
				.map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public MedicalRecordNoteDto findNoteById(UUID patientId, UUID noteId) {
		return toDto(findNoteEntityByPatientIdAndNoteId(patientId, noteId));
	}

	@Transactional
	public MedicalRecordNoteDto updateNote(UUID patientId, UUID noteId, MedicalRecordNoteUpdateDto request) {
		MedicalRecordNoteEntity note = findNoteEntityByPatientIdAndNoteId(patientId, noteId);
		note.setNote(request.note());

		return toDto(noteRepository.save(note));
	}

	@Transactional
	public void deleteNote(UUID patientId, UUID noteId) {
		MedicalRecordNoteEntity note = findNoteEntityByPatientIdAndNoteId(patientId, noteId);
		noteRepository.delete(note);
	}

	@Transactional
	public MedicalRecordAttachmentDto addAttachment(UUID patientId, MedicalRecordAttachmentCreateDto request) {
		return addAttachment(patientId, null, request);
	}

	@Transactional
	public MedicalRecordAttachmentDto addAttachment(UUID patientId, UUID createdByUserId,
			MedicalRecordAttachmentCreateDto request) {
		MedicalRecordEntity medicalRecord = findEntityByPatientId(patientId);
		StoredFileEntity storedFile = findStoredFile(request.storedFileId());
		if (attachmentRepository.existsByStoredFileId(request.storedFileId())) {
			throw new AppException(HttpStatus.CONFLICT, "STORED_FILE_ALREADY_ATTACHED",
					"Stored file already attached to a medical record");
		}

		MedicalRecordAttachmentEntity attachment = new MedicalRecordAttachmentEntity();
		attachment.setMedicalRecordId(medicalRecord.getId());
		attachment.setStoredFileId(request.storedFileId());
		attachment.setDescription(request.description());

		return toDto(attachmentRepository.save(attachment), storedFile);
	}

	@Transactional(readOnly = true)
	public List<MedicalRecordAttachmentDto> findAttachmentsByPatientId(UUID patientId) {
		MedicalRecordEntity medicalRecord = findEntityByPatientId(patientId);

		return attachmentRepository.findAllByMedicalRecordIdOrderByCreatedAtDesc(medicalRecord.getId()).stream()
				.map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public MedicalRecordAttachmentDto findAttachmentById(UUID patientId, UUID attachmentId) {
		return toDto(findAttachmentEntityByPatientIdAndAttachmentId(patientId, attachmentId));
	}

	@Transactional
	public MedicalRecordAttachmentDto updateAttachment(UUID patientId, UUID attachmentId,
			MedicalRecordAttachmentUpdateDto request) {
		MedicalRecordAttachmentEntity attachment = findAttachmentEntityByPatientIdAndAttachmentId(patientId,
				attachmentId);
		attachment.setDescription(request.description());

		return toDto(attachmentRepository.save(attachment));
	}

	@Transactional
	public void deleteAttachment(UUID patientId, UUID attachmentId) {
		MedicalRecordAttachmentEntity attachment = findAttachmentEntityByPatientIdAndAttachmentId(patientId,
				attachmentId);
		attachmentRepository.delete(attachment);
	}

	private MedicalRecordEntity findEntityByPatientId(UUID patientId) {
		if (!patientRepository.existsById(patientId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", "Patient not found");
		}

		return medicalRecordRepository.findByPatientId(patientId).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "MEDICAL_RECORD_NOT_FOUND", MEDICAL_RECORD_NOT_FOUND));
	}

	private MedicalRecordNoteEntity findNoteEntityByPatientIdAndNoteId(UUID patientId, UUID noteId) {
		MedicalRecordEntity medicalRecord = findEntityByPatientId(patientId);

		return noteRepository.findByIdAndMedicalRecordId(noteId, medicalRecord.getId())
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "MEDICAL_RECORD_NOTE_NOT_FOUND",
						"Medical record note not found"));
	}

	private MedicalRecordAttachmentEntity findAttachmentEntityByPatientIdAndAttachmentId(UUID patientId,
			UUID attachmentId) {
		MedicalRecordEntity medicalRecord = findEntityByPatientId(patientId);

		return attachmentRepository.findByIdAndMedicalRecordId(attachmentId, medicalRecord.getId())
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "MEDICAL_RECORD_ATTACHMENT_NOT_FOUND",
						"Medical record attachment not found"));
	}

	private StoredFileEntity findStoredFile(UUID storedFileId) {
		return storedFileRepository.findById(storedFileId).orElseThrow(
				() -> new AppException(HttpStatus.BAD_REQUEST, "STORED_FILE_NOT_FOUND", "Stored file not found"));
	}

	private MedicalRecordDto toDto(MedicalRecordEntity entity) {
		return new MedicalRecordDto(entity.getId(), entity.getPatientId(), entity.getCreatedByUserId(),
				entity.getAllergies(), entity.getChronicConditions(), entity.getContinuousMedications(),
				entity.getGeneralObservations(), entity.getCreatedAt(), entity.getUpdatedAt());
	}

	private MedicalRecordNoteDto toDto(MedicalRecordNoteEntity entity) {
		return new MedicalRecordNoteDto(entity.getId(), entity.getMedicalRecordId(), entity.getCreatedByUserId(),
				entity.getNote(), entity.getCreatedAt());
	}

	private MedicalRecordAttachmentDto toDto(MedicalRecordAttachmentEntity entity) {
		return toDto(entity, findStoredFile(entity.getStoredFileId()));
	}

	private MedicalRecordAttachmentDto toDto(MedicalRecordAttachmentEntity entity, StoredFileEntity storedFile) {
		return new MedicalRecordAttachmentDto(entity.getId(), entity.getMedicalRecordId(), entity.getStoredFileId(),
				storedFile.getOriginalFileName(), storedFile.getMimeType(), storedFile.getSizeBytes(),
				entity.getDescription(), entity.getCreatedAt());
	}
}
