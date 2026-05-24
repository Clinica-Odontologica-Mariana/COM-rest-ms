package com.clinica.mariana.restms.storedfile.unit;

import com.clinica.mariana.restms.storedfile.entity.StoredFileEntity;
import com.clinica.mariana.restms.storedfile.model.FileCategory;
import com.clinica.mariana.restms.storedfile.repository.StoredFileRepository;
import com.clinica.mariana.restms.storedfile.service.FileNameSanitizer;
import com.clinica.mariana.restms.storedfile.service.FileValidationService;
import com.clinica.mariana.restms.storedfile.service.MinioStorageService;
import com.clinica.mariana.restms.storedfile.service.StoredFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Stored file service")
class StoredFileServiceTest {

	@Mock
	private StoredFileRepository storedFileRepository;

	@Mock
	private MinioStorageService minioStorageService;

	@Mock
	private FileValidationService fileValidationService;

	@Test
	void shouldUploadFileWithSafeObjectKeyAndMetadata() {
		StoredFileService service = new StoredFileService(storedFileRepository, minioStorageService,
				fileValidationService, new FileNameSanitizer());
		UUID ownerId = UUID.randomUUID();
		MockMultipartFile file = new MockMultipartFile("file", "../profile photo.png", "image/png",
				new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
		when(minioStorageService.upload(any(), any(), eq("image/png"))).thenAnswer(
				invocation -> new MinioStorageService.UploadResult("bucket", invocation.getArgument(1), null));
		when(storedFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		StoredFileEntity result = service.upload(file, FileCategory.USER_PROFILE_PHOTO, ownerId, ownerId, null);

		ArgumentCaptor<String> objectKeyCaptor = ArgumentCaptor.forClass(String.class);
		verify(minioStorageService).upload(any(), objectKeyCaptor.capture(), eq("image/png"));
		assertThat(objectKeyCaptor.getValue()).startsWith("profile-photos/" + ownerId + "/");
		assertThat(objectKeyCaptor.getValue()).endsWith("-profile-photo.png");
		assertThat(result.getOriginalFileName()).isEqualTo("profile-photo.png");
		assertThat(result.getFileCategory()).isEqualTo(FileCategory.USER_PROFILE_PHOTO);
		assertThat(result.getChecksumSha256()).hasSize(64);
	}
}
