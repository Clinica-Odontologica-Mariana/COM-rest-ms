package com.clinica.mariana.restms.certificate.unit;

import com.clinica.mariana.restms.certificate.entity.CertificateEntity;
import com.clinica.mariana.restms.certificate.repository.CertificateRepository;
import com.clinica.mariana.restms.certificate.service.CertificateService;
import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;
import com.clinica.mariana.restms.storedfile.repository.StoredFileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

	@Mock
	private CertificateRepository repo;
	@Mock
	private ProfessionalRepository professionalRepository;
	@Mock
	private StoredFileRepository storedFileRepository;
	@InjectMocks
	private CertificateService service;

	@Test
	void setFeatured_whenLimitNotReached_savesAndReturns() {
		CertificateEntity entity = activeEntity(false);
		when(repo.findByIdAndActiveTrue(entity.getId())).thenReturn(Optional.of(entity));
		when(repo.countByFeaturedTrueAndActiveTrue()).thenReturn(2L);
		when(repo.save(any())).thenReturn(entity);

		service.setFeatured(entity.getId(), true);

		verify(repo).save(argThat(CertificateEntity::isFeatured));
	}

	@Test
	void setFeatured_whenLimitReached_throws422() {
		CertificateEntity entity = activeEntity(false);
		when(repo.findByIdAndActiveTrue(entity.getId())).thenReturn(Optional.of(entity));
		when(repo.countByFeaturedTrueAndActiveTrue()).thenReturn(3L);

		assertThatThrownBy(() -> service.setFeatured(entity.getId(), true)).isInstanceOf(AppException.class)
				.extracting("code").isEqualTo("FEATURED_LIMIT_REACHED");

		verify(repo, never()).save(any());
	}

	@Test
	void setFeatured_removeFeatured_alwaysSucceeds() {
		CertificateEntity entity = activeEntity(true);
		when(repo.findByIdAndActiveTrue(entity.getId())).thenReturn(Optional.of(entity));
		when(repo.save(any())).thenReturn(entity);

		service.setFeatured(entity.getId(), false);

		verify(repo).save(argThat(e -> !e.isFeatured()));
	}

	@Test
	void setFeatured_whenAlreadyAtTargetValue_doesNotSave() {
		CertificateEntity entity = activeEntity(true);
		when(repo.findByIdAndActiveTrue(entity.getId())).thenReturn(Optional.of(entity));

		service.setFeatured(entity.getId(), true);

		verify(repo, never()).countByFeaturedTrueAndActiveTrue();
		verify(repo, never()).save(any());
	}

	@Test
	void delete_clearsFeaturedFlag() {
		CertificateEntity entity = activeEntity(true);
		when(repo.findById(entity.getId())).thenReturn(Optional.of(entity));
		when(repo.save(any())).thenReturn(entity);

		service.delete(entity.getId());

		verify(repo).save(argThat(e -> !e.isFeatured() && !e.isActive()));
	}

	private CertificateEntity activeEntity(boolean featured) {
		CertificateEntity entity = new CertificateEntity();
		entity.setId(UUID.randomUUID());
		entity.setTitle("Certificado");
		entity.setCertificateType("ATTENDANCE");
		entity.setIssuedAt(OffsetDateTime.now());
		entity.setActive(true);
		entity.setFeatured(featured);
		return entity;
	}
}
