package com.clinica.mariana.restms.medicalrecord.service;

import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteUpdateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordUpdateDto;
import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordAttachmentEntity;
import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordEntity;
import com.clinica.mariana.restms.medicalrecord.entity.MedicalRecordNoteEntity;
import com.clinica.mariana.restms.medicalrecord.model.MedicalRecordModel;
import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordAttachmentRepository;
import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordNoteRepository;
import com.clinica.mariana.restms.medicalrecord.repository.MedicalRecordRepository;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import com.clinica.mariana.restms.storedfile.repository.StoredFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class MedicalRecordService {

	private final MedicalRecordRepository medicalRecordRepository;
	private final MedicalRecordNoteRepository noteRepository;
	private final MedicalRecordAttachmentRepository attachmentRepository;
	private final PatientRepository patientRepository;
	private final StoredFileRepository storedFileRepository;

	public MedicalRecordService(
			MedicalRecordRepository medicalRecordRepository,
			MedicalRecordNoteRepository noteRepository,
			MedicalRecordAttachmentRepository attachmentRepository,
			PatientRepository patientRepository,
			StoredFileRepository storedFileRepository
	) {
		this.medicalRecordRepository = medicalRecordRepository;
		this.noteRepository = noteRepository;
		this.attachmentRepository = attachmentRepository;
		this.patientRepository = patientRepository;
		this.storedFileRepository = storedFileRepository;
	}

	@Transactional
	public MedicalRecordDto create(MedicalRecordCreateDto request) {
		if (!patientRepository.existsById(request.patientId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found");
		}

		if (medicalRecordRepository.existsByPatientId(request.patientId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Medical record already exists for patient");
		}

		MedicalRecordEntity entity = new MedicalRecordEntity();
		entity.setPatientId(request.patientId());
		entity.setAllergies(request.allergies());
		entity.setChronicConditions(request.chronicConditions());
		entity.setContinuousMedications(request.continuousMedications());
		entity.setGeneralObservations(request.generalObservations());

		return toDto(toModel(medicalRecordRepository.save(entity)));
	}

	@Transactional
	public MedicalRecordDto createForPatientIfMissing(UUID patientId, UUID createdByUserId) {
		return medicalRecordRepository.findByPatientId(patientId)
				.map(this::toModel)
				.map(this::toDto)
				.orElseGet(() -> {
					MedicalRecordEntity entity = new MedicalRecordEntity();
					entity.setPatientId(patientId);
					entity.setCreatedByUserId(createdByUserId);
					return toDto(toModel(medicalRecordRepository.save(entity)));
				});
	}

	@Transactional(readOnly = true)
	public List<MedicalRecordDto> findAll() {
		return medicalRecordRepository.findAllByOrderByUpdatedAtDesc()
				.stream()
				.map(this::toModel)
				.map(this::toDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public MedicalRecordDto findById(UUID id) {
		MedicalRecordEntity entity = medicalRecordRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found"));

		return toDto(toModel(entity));
	}

	@Transactional(readOnly = true)
	public MedicalRecordDto findByPatientId(UUID patientId) {
		MedicalRecordEntity entity = findEntityByPatientId(patientId);

		return toDto(toModel(entity));
	}

	@Transactional
	public MedicalRecordDto update(UUID id, MedicalRecordUpdateDto request) {
		MedicalRecordEntity entity = medicalRecordRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found"));

		entity.setAllergies(request.allergies());
		entity.setChronicConditions(request.chronicConditions());
		entity.setContinuousMedications(request.continuousMedications());
		entity.setGeneralObservations(request.generalObservations());

		return toDto(toModel(medicalRecordRepository.save(entity)));
	}

	@Transactional
	public void delete(UUID id) {
		MedicalRecordEntity entity = medicalRecordRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found"));

		medicalRecordRepository.delete(entity);
	}

	@Transactional
	public MedicalRecordNoteDto addNote(UUID patientId, MedicalRecordNoteCreateDto request) {
		return addNote(patientId, null, request);
	}

	@Transactional
	public MedicalRecordNoteDto addNote(UUID patientId, UUID createdByUserId, MedicalRecordNoteCreateDto request) {
		MedicalRecordEntity record = findEntityByPatientId(patientId);

		MedicalRecordNoteEntity note = new MedicalRecordNoteEntity();
		note.setMedicalRecordId(record.getId());
		note.setCreatedByUserId(createdByUserId);
		note.setNote(request.note());

		return toDto(noteRepository.save(note));
	}

	@Transactional(readOnly = true)
	public List<MedicalRecordNoteDto> findNotesByPatientId(UUID patientId) {
		MedicalRecordEntity record = findEntityByPatientId(patientId);

		return noteRepository.findAllByMedicalRecordIdOrderByCreatedAtDesc(record.getId())
				.stream()
				.map(this::toDto)
				.toList();
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
		MedicalRecordEntity record = findEntityByPatientId(patientId);
		if (!storedFileRepository.existsById(request.storedFileId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stored file not found");
		}

		MedicalRecordAttachmentEntity attachment = new MedicalRecordAttachmentEntity();
		attachment.setMedicalRecordId(record.getId());
		attachment.setStoredFileId(request.storedFileId());
		attachment.setDescription(request.description());

		return toDto(attachmentRepository.save(attachment));
	}

	private MedicalRecordEntity findEntityByPatientId(UUID patientId) {
		if (!patientRepository.existsById(patientId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found");
		}

		return medicalRecordRepository.findByPatientId(patientId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found"));
	}

	private MedicalRecordNoteEntity findNoteEntityByPatientIdAndNoteId(UUID patientId, UUID noteId) {
		MedicalRecordEntity record = findEntityByPatientId(patientId);

		return noteRepository.findByIdAndMedicalRecordId(noteId, record.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record note not found"));
	}

	private MedicalRecordModel toModel(MedicalRecordEntity entity) {
		return new MedicalRecordModel(
				entity.getId(),
				entity.getPatientId(),
				entity.getCreatedByUserId(),
				entity.getAllergies(),
				entity.getChronicConditions(),
				entity.getContinuousMedications(),
				entity.getGeneralObservations(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	private MedicalRecordDto toDto(MedicalRecordModel model) {
		return new MedicalRecordDto(
				model.id(),
				model.patientId(),
				model.createdByUserId(),
				model.allergies(),
				model.chronicConditions(),
				model.continuousMedications(),
				model.generalObservations(),
				model.createdAt(),
				model.updatedAt()
		);
	}

	private MedicalRecordNoteDto toDto(MedicalRecordNoteEntity entity) {
		return new MedicalRecordNoteDto(
				entity.getId(),
				entity.getMedicalRecordId(),
				entity.getCreatedByUserId(),
				entity.getNote(),
				entity.getCreatedAt()
		);
	}

	private MedicalRecordAttachmentDto toDto(MedicalRecordAttachmentEntity entity) {
		return new MedicalRecordAttachmentDto(
				entity.getId(),
				entity.getMedicalRecordId(),
				entity.getStoredFileId(),
				entity.getDescription(),
				entity.getCreatedAt()
		);
	}
}
