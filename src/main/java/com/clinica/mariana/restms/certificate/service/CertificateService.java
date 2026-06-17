package com.clinica.mariana.restms.certificate.service;

import com.clinica.mariana.restms.certificate.dto.CertificateCreateDto;
import com.clinica.mariana.restms.certificate.dto.CertificateDto;
import com.clinica.mariana.restms.certificate.dto.CertificateUpdateDto;
import com.clinica.mariana.restms.certificate.entity.CertificateEntity;
import com.clinica.mariana.restms.certificate.repository.CertificateRepository;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;
import com.clinica.mariana.restms.storedfile.repository.StoredFileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clinica.mariana.restms.common.exception.AppException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class CertificateService {

	private static final String CERTIFICATE_NOT_FOUND = "Certificate not found";

	private final CertificateRepository certificateRepository;
	private final ProfessionalRepository professionalRepository;
	private final StoredFileRepository storedFileRepository;

	public CertificateService(CertificateRepository certificateRepository,
			ProfessionalRepository professionalRepository, StoredFileRepository storedFileRepository) {
		this.certificateRepository = certificateRepository;
		this.professionalRepository = professionalRepository;
		this.storedFileRepository = storedFileRepository;
	}

	@Transactional
	public CertificateDto create(CertificateCreateDto request) {
		validateReferences(request.professionalId(), request.storedFileId());
		CertificateEntity entity = new CertificateEntity();
		apply(entity, request.professionalId(), request.title(), request.certificateType(), request.content(),
				request.issuedAt(), request.storedFileId());
		entity.setActive(true);
		entity.setRevokedAt(null);
		return toDto(certificateRepository.save(entity));
	}

	@Transactional(readOnly = true)
	public Page<CertificateDto> findAll(Pageable pageable) {
		return certificateRepository.findAllByActiveTrueOrderByIssuedAtDesc(pageable).map(this::toDto);
	}

	@Transactional(readOnly = true)
	public CertificateDto findById(UUID id) {
		return toDto(findActiveEntity(id));
	}

	@Transactional
	public CertificateDto update(UUID id, CertificateUpdateDto request) {
		CertificateEntity entity = findActiveEntity(id);
		validateReferences(request.professionalId(), request.storedFileId());
		apply(entity, request.professionalId(), request.title(), request.certificateType(), request.content(),
				request.issuedAt(), request.storedFileId());
		return toDto(certificateRepository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		CertificateEntity entity = findEntity(id);
		if (!entity.isActive()) {
			return;
		}
		entity.setActive(false);
		entity.setRevokedAt(OffsetDateTime.now());
		certificateRepository.save(entity);
	}

	private CertificateEntity findEntity(UUID id) {
		return certificateRepository.findById(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "CERTIFICATE_NOT_FOUND", CERTIFICATE_NOT_FOUND));
	}

	private CertificateEntity findActiveEntity(UUID id) {
		return certificateRepository.findByIdAndActiveTrue(id).orElseThrow(
				() -> new AppException(HttpStatus.NOT_FOUND, "CERTIFICATE_NOT_FOUND", CERTIFICATE_NOT_FOUND));
	}

	private void validateReferences(UUID professionalId, UUID storedFileId) {
		if (professionalId != null && !professionalRepository.existsById(professionalId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "PROFESSIONAL_NOT_FOUND", "Professional not found");
		}
		if (storedFileId != null && !storedFileRepository.existsById(storedFileId)) {
			throw new AppException(HttpStatus.NOT_FOUND, "STORED_FILE_NOT_FOUND", "Stored file not found");
		}
	}

	private void apply(CertificateEntity entity, UUID professionalId, String title, String certificateType,
			String content, OffsetDateTime issuedAt, UUID storedFileId) {
		entity.setProfessionalId(professionalId);
		entity.setTitle(title);
		entity.setCertificateType(certificateType);
		entity.setContent(content);
		entity.setIssuedAt(issuedAt == null ? OffsetDateTime.now() : issuedAt);
		entity.setStoredFileId(storedFileId);
	}

	private CertificateDto toDto(CertificateEntity entity) {
		return new CertificateDto(entity.getId(), entity.getProfessionalId(), entity.getTitle(),
				entity.getCertificateType(), entity.getContent(), entity.getIssuedAt(), entity.getStoredFileId(),
				entity.isActive(), entity.getRevokedAt(), entity.getCreatedAt(), entity.getUpdatedAt());
	}
}
