package com.clinica.mariana.restms.medicalrecord.test;

import com.clinica.mariana.restms.medicalrecord.controller.MedicalRecordController;
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
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.OffsetDateTime;
import java.time.Instant;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MedicalRecordControllerTest {

	private final MedicalRecordService medicalRecordService = mock(MedicalRecordService.class);
	private final MedicalRecordController controller = new MedicalRecordController(medicalRecordService);

	@Test
	void shouldDelegateRequestsToService() {
		UUID id = UUID.randomUUID();
		UUID patientId = UUID.randomUUID();
		UUID noteId = UUID.randomUUID();
		UUID attachmentId = UUID.randomUUID();
		UUID storedFileId = UUID.randomUUID();
		UUID createdByUserId = UUID.randomUUID();
		MedicalRecordDto dto = dto(id, patientId);
		MedicalRecordNoteDto noteDto = noteDto(noteId, id);
		MedicalRecordAttachmentDto attachmentDto = attachmentDto(attachmentId, id, storedFileId);
		MedicalRecordCreateDto createDto = new MedicalRecordCreateDto(patientId, "Alergia", null, null, null);
		MedicalRecordUpdateDto updateDto = new MedicalRecordUpdateDto("Nenhuma", null, null, null);
		MedicalRecordNoteCreateDto noteCreateDto = new MedicalRecordNoteCreateDto("Nota inicial");
		MedicalRecordNoteUpdateDto noteUpdateDto = new MedicalRecordNoteUpdateDto("Nota atualizada");
		MedicalRecordAttachmentCreateDto attachmentCreateDto = new MedicalRecordAttachmentCreateDto(storedFileId,
				"Radiografia inicial");
		MedicalRecordAttachmentUpdateDto attachmentUpdateDto = new MedicalRecordAttachmentUpdateDto(
				"Radiografia revisada");

		when(medicalRecordService.create(createDto)).thenReturn(dto);
		when(medicalRecordService.findAll(any())).thenReturn(new PageImpl<>(List.of(dto)));
		when(medicalRecordService.findById(id)).thenReturn(dto);
		when(medicalRecordService.findByPatientId(patientId)).thenReturn(dto);
		when(medicalRecordService.update(id, updateDto)).thenReturn(dto);
		when(medicalRecordService.addNote(patientId, noteCreateDto)).thenReturn(noteDto);
		when(medicalRecordService.findNotesByPatientId(patientId)).thenReturn(List.of(noteDto));
		when(medicalRecordService.findNoteById(patientId, noteId)).thenReturn(noteDto);
		when(medicalRecordService.updateNote(patientId, noteId, noteUpdateDto)).thenReturn(noteDto);
		when(medicalRecordService.addAttachment(patientId, attachmentCreateDto)).thenReturn(attachmentDto);
		when(medicalRecordService.findAttachmentsByPatientId(patientId)).thenReturn(List.of(attachmentDto));
		when(medicalRecordService.findAttachmentById(patientId, attachmentId)).thenReturn(attachmentDto);
		when(medicalRecordService.updateAttachment(patientId, attachmentId, attachmentUpdateDto))
				.thenReturn(attachmentDto);

		assertThat(controller.create(createDto)).isEqualTo(dto);
		assertThat(controller.findAll(Pageable.unpaged()).stream().toList()).containsExactly(dto);
		assertThat(controller.findById(id)).isEqualTo(dto);
		assertThat(controller.findByPatientId(patientId)).isEqualTo(dto);
		assertThat(controller.update(id, updateDto)).isEqualTo(dto);
		assertThat(controller.addNote(patientId, noteCreateDto, jwt(createdByUserId))).isEqualTo(noteDto);
		assertThat(controller.findNotesByPatientId(patientId)).containsExactly(noteDto);
		assertThat(controller.findNoteById(patientId, noteId)).isEqualTo(noteDto);
		assertThat(controller.updateNote(patientId, noteId, noteUpdateDto)).isEqualTo(noteDto);
		assertThat(controller.addAttachment(patientId, attachmentCreateDto)).isEqualTo(attachmentDto);
		assertThat(controller.findAttachmentsByPatientId(patientId)).containsExactly(attachmentDto);
		assertThat(controller.findAttachmentById(patientId, attachmentId)).isEqualTo(attachmentDto);
		assertThat(controller.updateAttachment(patientId, attachmentId, attachmentUpdateDto)).isEqualTo(attachmentDto);

		controller.delete(id);
		verify(medicalRecordService).delete(id);

		controller.deleteNote(patientId, noteId);
		verify(medicalRecordService).deleteNote(patientId, noteId);

		controller.deleteAttachment(patientId, attachmentId);
		verify(medicalRecordService).deleteAttachment(patientId, attachmentId);
	}

	@Test
	void shouldHandleNullJwtInAddNote() {
		UUID patientId = UUID.randomUUID();
		MedicalRecordNoteCreateDto noteCreateDto = new MedicalRecordNoteCreateDto("Nota");
		MedicalRecordNoteDto noteDto = noteDto(UUID.randomUUID(), UUID.randomUUID());

		when(medicalRecordService.addNote(patientId, noteCreateDto)).thenReturn(noteDto);

		assertThat(controller.addNote(patientId, noteCreateDto, null)).isEqualTo(noteDto);

		verify(medicalRecordService).addNote(patientId, noteCreateDto);
	}

	@Test
	void shouldHandleInvalidUuidInSubject() {
		UUID patientId = UUID.randomUUID();
		MedicalRecordNoteCreateDto noteCreateDto = new MedicalRecordNoteCreateDto("Nota");
		MedicalRecordNoteDto noteDto = noteDto(UUID.randomUUID(), UUID.randomUUID());

		when(medicalRecordService.addNote(patientId, noteCreateDto)).thenReturn(noteDto);

		assertThat(controller.addNote(patientId, noteCreateDto, jwtWithInvalidSubject())).isEqualTo(noteDto);

		verify(medicalRecordService).addNote(patientId, noteCreateDto);
	}

	private MedicalRecordDto dto(UUID id, UUID patientId) {
		return new MedicalRecordDto(id, patientId, UUID.randomUUID(), "Alergia", "Condicao", "Medicacao", "Observacao",
				OffsetDateTime.now(), OffsetDateTime.now());
	}

	private Jwt jwt(UUID appUserId) {
		return Jwt.withTokenValue("token").header("alg", "none").subject(appUserId.toString()).issuedAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(60)).build();
	}

	private Jwt jwtWithInvalidSubject() {
		return Jwt.withTokenValue("token").header("alg", "none").subject("not-a-valid-uuid").issuedAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(60)).build();
	}

	private MedicalRecordNoteDto noteDto(UUID id, UUID medicalRecordId) {
		return new MedicalRecordNoteDto(id, medicalRecordId, UUID.randomUUID(), "Nota inicial", OffsetDateTime.now());
	}

	private MedicalRecordAttachmentDto attachmentDto(UUID id, UUID medicalRecordId, UUID storedFileId) {
		return new MedicalRecordAttachmentDto(id, medicalRecordId, storedFileId, "radiografia.png", "image/png", 2048L,
				"Radiografia inicial", OffsetDateTime.now());
	}
}
