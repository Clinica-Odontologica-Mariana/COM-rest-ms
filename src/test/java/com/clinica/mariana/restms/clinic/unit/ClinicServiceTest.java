package com.clinica.mariana.restms.clinic.unit;

import com.clinica.mariana.restms.address.dto.AddressCreateDto;
import com.clinica.mariana.restms.address.entity.AddressEntity;
import com.clinica.mariana.restms.address.repository.AddressRepository;
import com.clinica.mariana.restms.clinic.dto.ClinicCreateDto;
import com.clinica.mariana.restms.clinic.entity.ClinicEntity;
import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
import com.clinica.mariana.restms.clinic.repository.EquipmentRepository;
import com.clinica.mariana.restms.clinic.repository.SocialLinkRepository;
import com.clinica.mariana.restms.clinic.repository.WorkingHoursRepository;
import com.clinica.mariana.restms.clinic.service.ClinicService;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;
import com.clinica.mariana.restms.storedfile.dto.PresignedUrlDto;
import com.clinica.mariana.restms.storedfile.entity.StoredFileEntity;
import com.clinica.mariana.restms.storedfile.model.FileCategory;
import com.clinica.mariana.restms.storedfile.service.StoredFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Clinic service")
class ClinicServiceTest {

	@Mock
	private ClinicRepository clinicRepository;

	@Mock
	private AddressRepository addressRepository;

	@Mock
	private StoredFileService storedFileService;

	@Mock
	private WorkingHoursRepository workingHoursRepository;

	@Mock
	private SocialLinkRepository socialLinkRepository;

	@Mock
	private EquipmentRepository equipmentRepository;

	@Mock
	private ProfessionalRepository professionalRepository;

	@Test
	void shouldCreateClinicWithEmbeddedAddress() {
		ClinicService service = service();
		UUID addressId = UUID.randomUUID();
		AddressEntity address = new AddressEntity();
		address.setId(addressId);
		address.setStreet("Rua das Flores");
		address.setNumber("123");
		address.setNeighborhood("Centro");
		address.setCity("Brasilia");
		address.setState("DF");
		address.setZipCode("70000000");
		when(addressRepository.save(any())).thenReturn(address);
		when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
		when(clinicRepository.save(any())).thenAnswer(invocation -> {
			ClinicEntity entity = invocation.getArgument(0);
			entity.setId(UUID.randomUUID());
			return entity;
		});

		var result = service.create(new ClinicCreateDto(null, "Clínica Jardim", "11999999999", "jardim@clinic.com",
				null, "11988888888", "@clinicajardim", "temporary", java.time.LocalDate.parse("2026-06-01"),
				java.time.LocalDate.parse("2026-06-10"),
				new AddressCreateDto("Rua das Flores", "123", null, "Centro", "Brasilia", "DF", "70000000")));

		assertThat(result.id()).isNotNull();
		assertThat(result.addressId()).isEqualTo(addressId);
		assertThat(result.address()).isNotNull();
		assertThat(result.address().street()).isEqualTo("Rua das Flores");
		assertThat(result.timezone()).isEqualTo("America/Sao_Paulo");
		assertThat(result.whatsapp()).isEqualTo("11988888888");
		assertThat(result.instagram()).isEqualTo("@clinicajardim");
		assertThat(result.inactiveType()).isEqualTo("temporary");
	}

	@Test
	void shouldUploadClinicPhotoToStoredFilesAndReturnPresignedUrl() {
		ClinicService service = service();
		UUID clinicId = UUID.randomUUID();
		UUID fileId = UUID.randomUUID();
		ClinicEntity clinic = activeClinic(clinicId, null);
		StoredFileEntity storedFile = storedFile(fileId);
		MockMultipartFile file = new MockMultipartFile("file", "clinic.png", "image/png",
				new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
		when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
		when(storedFileService.upload(file, FileCategory.CLINIC_PHOTO, clinicId, null, "Clinic photo"))
				.thenReturn(storedFile);
		when(clinicRepository.saveAndFlush(clinic)).thenReturn(clinic);
		when(storedFileService.presignedDownloadUrl(fileId, FileCategory.CLINIC_PHOTO))
				.thenReturn(new PresignedUrlDto("https://minio.local/clinic.png", OffsetDateTime.now().plusMinutes(5)));

		var result = service.uploadPhoto(clinicId, file);

		assertThat(result.clinicPhotoFileId()).isEqualTo(fileId);
		assertThat(result.clinicPhotoUrl()).isEqualTo("https://minio.local/clinic.png");
		verify(storedFileService).upload(file, FileCategory.CLINIC_PHOTO, clinicId, null, "Clinic photo");
	}

	@Test
	void shouldReplacePreviousClinicPhoto() {
		ClinicService service = service();
		UUID clinicId = UUID.randomUUID();
		UUID oldFileId = UUID.randomUUID();
		UUID newFileId = UUID.randomUUID();
		ClinicEntity clinic = activeClinic(clinicId, oldFileId);
		StoredFileEntity oldFile = storedFile(oldFileId);
		StoredFileEntity newFile = storedFile(newFileId);
		MockMultipartFile file = new MockMultipartFile("file", "new-clinic.png", "image/png",
				new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
		when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
		when(storedFileService.upload(file, FileCategory.CLINIC_PHOTO, clinicId, null, "Clinic photo"))
				.thenReturn(newFile);
		when(clinicRepository.saveAndFlush(clinic)).thenReturn(clinic);
		when(storedFileService.findActiveByIdAndCategory(oldFileId, FileCategory.CLINIC_PHOTO)).thenReturn(oldFile);
		when(storedFileService.presignedDownloadUrl(newFileId, FileCategory.CLINIC_PHOTO)).thenReturn(
				new PresignedUrlDto("https://minio.local/new-clinic.png", OffsetDateTime.now().plusMinutes(5)));

		var result = service.uploadPhoto(clinicId, file);

		assertThat(result.clinicPhotoFileId()).isEqualTo(newFileId);
		verify(storedFileService).hardDelete(oldFile);
	}

	@Test
	void shouldDeleteClinicPhoto() {
		ClinicService service = service();
		UUID clinicId = UUID.randomUUID();
		UUID fileId = UUID.randomUUID();
		ClinicEntity clinic = activeClinic(clinicId, fileId);
		StoredFileEntity storedFile = storedFile(fileId);
		when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
		when(clinicRepository.saveAndFlush(clinic)).thenReturn(clinic);
		when(storedFileService.findActiveByIdAndCategory(fileId, FileCategory.CLINIC_PHOTO)).thenReturn(storedFile);

		var result = service.deletePhoto(clinicId);

		assertThat(result.clinicPhotoFileId()).isNull();
		assertThat(result.clinicPhotoUrl()).isNull();
		verify(storedFileService).hardDelete(storedFile);
	}

	@Test
	void shouldDeleteClinicAndAssociatedPhoto() {
		ClinicService service = service();
		UUID clinicId = UUID.randomUUID();
		UUID fileId = UUID.randomUUID();
		ClinicEntity clinic = activeClinic(clinicId, fileId);
		StoredFileEntity storedFile = storedFile(fileId);
		when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
		when(storedFileService.findActiveByIdAndCategory(fileId, FileCategory.CLINIC_PHOTO)).thenReturn(storedFile);

		service.delete(clinicId);

		verify(workingHoursRepository).deleteAllByClinicId(clinicId);
		verify(socialLinkRepository).deleteAllByClinicId(clinicId);
		verify(equipmentRepository).deleteAllByClinicId(clinicId);
		verify(clinicRepository).delete(clinic);
		verify(clinicRepository).flush();
		verify(storedFileService).hardDelete(storedFile);
	}

	private ClinicService service() {
		return new ClinicService(clinicRepository, addressRepository, workingHoursRepository, socialLinkRepository,
				equipmentRepository, professionalRepository, storedFileService);
	}

	private ClinicEntity activeClinic(UUID id, UUID photoFileId) {
		ClinicEntity entity = new ClinicEntity();
		entity.setId(id);
		entity.setName("Clínica Jardim");
		entity.setPhone("11999999999");
		entity.setEmail("jardim@clinic.com");
		entity.setTimezone("America/Sao_Paulo");
		entity.setActive(true);
		entity.setClinicPhotoFileId(photoFileId);
		return entity;
	}

	private StoredFileEntity storedFile(UUID id) {
		StoredFileEntity entity = new StoredFileEntity();
		entity.setId(id);
		entity.setFileCategory(FileCategory.CLINIC_PHOTO);
		entity.setObjectKey("clinic-photos/clinic/file.png");
		return entity;
	}
}
