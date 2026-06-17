package com.clinica.mariana.restms.certificate.repository;

import com.clinica.mariana.restms.certificate.entity.CertificateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<CertificateEntity, UUID> {

	Page<CertificateEntity> findAllByActiveTrueOrderByIssuedAtDesc(Pageable pageable);

	Optional<CertificateEntity> findByIdAndActiveTrue(UUID id);
}
