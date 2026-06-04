package com.clinica.mariana.restms.medicalrecord.test;

import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordAttachmentUpdateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteCreateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordNoteUpdateDto;
import com.clinica.mariana.restms.medicalrecord.dto.MedicalRecordUpdateDto;
import com.clinica.mariana.restms.medicalrecord.service.MedicalRecordService;
import com.clinica.mariana.restms.patient.entity.PatientEntity;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import com.clinica.mariana.restms.storedfile.entity.StoredFileEntity;
import com.clinica.mariana.restms.storedfile.model.FileCategory;
import com.clinica.mariana.restms.storedfile.repository.StoredFileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import com.clinica.mariana.restms.common.exception.AppException;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class MedicalRecordServiceTest {

	@Autowired
	private MedicalRecordService medicalRecordService;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private StoredFileRepository storedFileRepository;

	@Test
	void shouldRunMedicalRecordFlowAndValidateErrors() {
		PatientEntity patient = patientRepository.save(patient("Joao Prontuario", "98765432100"));

		MedicalRecordDto created = medicalRecordService.create(new MedicalRecordCreateDto(patient.getId(), "Penicilina",
				"Hipertensao", "Losartana", "Paciente em acompanhamento odontologico"));

		assertThat(created.id()).isNotNull();
		assertThat(created.patientId()).isEqualTo(patient.getId());

		assertThat(medicalRecordService.findAll(Pageable.unpaged()).getContent()).extracting(MedicalRecordDto::id)
				.contains(created.id());

		MedicalRecordDto foundById = medicalRecordService.findById(created.id());
		assertThat(foundById.allergies()).isEqualTo("Penicilina");

		MedicalRecordDto foundByPatient = medicalRecordService.findByPatientId(patient.getId());
		assertThat(foundByPatient.id()).isEqualTo(created.id());

		MedicalRecordDto updated = medicalRecordService.update(created.id(), new MedicalRecordUpdateDto(
				"Nenhuma alergia conhecida", "Hipertensao controlada", "Losartana 50mg", "Retorno em seis meses"));

		assertThat(updated.allergies()).isEqualTo("Nenhuma alergia conhecida");
		assertThat(updated.generalObservations()).isEqualTo("Retorno em seis meses");

		MedicalRecordNoteDto note = medicalRecordService.addNote(patient.getId(),
				new MedicalRecordNoteCreateDto("Evolucao inicial"));

		assertThat(note.id()).isNotNull();
		assertThat(note.medicalRecordId()).isEqualTo(created.id());
		assertThat(note.note()).isEqualTo("Evolucao inicial");

		assertThat(medicalRecordService.findNotesByPatientId(patient.getId())).extracting(MedicalRecordNoteDto::id)
				.contains(note.id());

		MedicalRecordNoteDto foundNote = medicalRecordService.findNoteById(patient.getId(), note.id());
		assertThat(foundNote.note()).isEqualTo("Evolucao inicial");

		MedicalRecordNoteDto updatedNote = medicalRecordService.updateNote(patient.getId(), note.id(),
				new MedicalRecordNoteUpdateDto("Evolucao atualizada"));
		assertThat(updatedNote.note()).isEqualTo("Evolucao atualizada");

		PatientEntity otherPatient = patientRepository.save(patient("Outro Prontuario", "98765432101"));
		medicalRecordService.create(new MedicalRecordCreateDto(otherPatient.getId(), null, null, null, null));

		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.findNoteById(otherPatient.getId(), note.id()));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.updateNote(otherPatient.getId(), note.id(),
				new MedicalRecordNoteUpdateDto("Nao deve atualizar")));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.deleteNote(otherPatient.getId(), note.id()));

		StoredFileEntity storedFile = storedFileRepository
				.save(storedFile("radiografia-inicial.png", "image/png", 2048L));
		MedicalRecordAttachmentDto attachment = medicalRecordService.addAttachment(patient.getId(),
				new MedicalRecordAttachmentCreateDto(storedFile.getId(), "Radiografia inicial"));

		assertThat(attachment.id()).isNotNull();
		assertThat(attachment.medicalRecordId()).isEqualTo(created.id());
		assertThat(attachment.storedFileId()).isEqualTo(storedFile.getId());
		assertThat(attachment.originalFileName()).isEqualTo("radiografia-inicial.png");
		assertThat(attachment.mimeType()).isEqualTo("image/png");
		assertThat(attachment.sizeBytes()).isEqualTo(2048L);
		assertThat(attachment.description()).isEqualTo("Radiografia inicial");

		assertThat(medicalRecordService.findAttachmentsByPatientId(patient.getId()))
				.extracting(MedicalRecordAttachmentDto::id).contains(attachment.id());

		MedicalRecordAttachmentDto foundAttachment = medicalRecordService.findAttachmentById(patient.getId(),
				attachment.id());
		assertThat(foundAttachment.originalFileName()).isEqualTo("radiografia-inicial.png");

		MedicalRecordAttachmentDto updatedAttachment = medicalRecordService.updateAttachment(patient.getId(),
				attachment.id(), new MedicalRecordAttachmentUpdateDto("Radiografia revisada"));
		assertThat(updatedAttachment.description()).isEqualTo("Radiografia revisada");

		assertStatus(HttpStatus.CONFLICT, () -> medicalRecordService.addAttachment(patient.getId(),
				new MedicalRecordAttachmentCreateDto(storedFile.getId(), "Duplicado")));
		assertStatus(HttpStatus.NOT_FOUND,
				() -> medicalRecordService.findAttachmentById(otherPatient.getId(), attachment.id()));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.updateAttachment(otherPatient.getId(),
				attachment.id(), new MedicalRecordAttachmentUpdateDto("Nao deve atualizar")));
		assertStatus(HttpStatus.NOT_FOUND,
				() -> medicalRecordService.deleteAttachment(otherPatient.getId(), attachment.id()));

		assertStatus(HttpStatus.CONFLICT,
				() -> medicalRecordService.create(new MedicalRecordCreateDto(patient.getId(), null, null, null, null)));

		UUID unknownId = UUID.randomUUID();
		assertStatus(HttpStatus.NOT_FOUND,
				() -> medicalRecordService.create(new MedicalRecordCreateDto(unknownId, null, null, null, null)));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.findById(unknownId));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.findByPatientId(unknownId));
		assertStatus(HttpStatus.NOT_FOUND,
				() -> medicalRecordService.update(unknownId, new MedicalRecordUpdateDto(null, null, null, null)));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.findNotesByPatientId(unknownId));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.findAttachmentsByPatientId(unknownId));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.findNoteById(patient.getId(), unknownId));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.updateNote(patient.getId(), unknownId,
				new MedicalRecordNoteUpdateDto("Nota inexistente")));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.deleteNote(patient.getId(), unknownId));
		assertStatus(HttpStatus.BAD_REQUEST, () -> medicalRecordService.addAttachment(patient.getId(),
				new MedicalRecordAttachmentCreateDto(unknownId, "Arquivo inexistente")));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.findAttachmentById(patient.getId(), unknownId));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.updateAttachment(patient.getId(), unknownId,
				new MedicalRecordAttachmentUpdateDto("Anexo inexistente")));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.deleteAttachment(patient.getId(), unknownId));

		medicalRecordService.deleteNote(patient.getId(), note.id());
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.findNoteById(patient.getId(), note.id()));

		medicalRecordService.deleteAttachment(patient.getId(), attachment.id());
		assertStatus(HttpStatus.NOT_FOUND,
				() -> medicalRecordService.findAttachmentById(patient.getId(), attachment.id()));

		medicalRecordService.delete(created.id());
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.findById(created.id()));
		assertStatus(HttpStatus.NOT_FOUND, () -> medicalRecordService.delete(created.id()));
	}

	private void assertStatus(HttpStatus status, Runnable action) {
		assertThatThrownBy(action::run).isInstanceOf(AppException.class).extracting(e -> ((AppException) e).getStatus())
				.isEqualTo(status);
	}

	private PatientEntity patient(String fullName, String cpf) {
		PatientEntity entity = new PatientEntity();
		entity.setFullName(fullName);
		entity.setCpf(cpf);
		entity.setPhone("61999999999");
		entity.setEmail(cpf + "@clinic.com");
		entity.setBirthDate(LocalDate.of(1985, 3, 20));
		entity.setActive(true);
		return entity;
	}

	private StoredFileEntity storedFile(String originalFileName, String mimeType, Long sizeBytes) {
		StoredFileEntity entity = new StoredFileEntity();
		ReflectionTestUtils.setField(entity, "bucketName", "medical-records");
		ReflectionTestUtils.setField(entity, "objectKey", UUID.randomUUID() + "/" + originalFileName);
		ReflectionTestUtils.setField(entity, "originalFileName", originalFileName);
		ReflectionTestUtils.setField(entity, "mimeType", mimeType);
		ReflectionTestUtils.setField(entity, "sizeBytes", sizeBytes);
		ReflectionTestUtils.setField(entity, "fileCategory", FileCategory.MEDICAL_RECORD_ATTACHMENT);
		return entity;
	}
}
