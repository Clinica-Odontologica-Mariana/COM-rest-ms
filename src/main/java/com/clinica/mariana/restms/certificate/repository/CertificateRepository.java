package com.clinica.mariana.restms.certificate.repository;

import com.clinica.mariana.restms.certificate.entity.CertificateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<CertificateEntity, UUID> {

	Page<CertificateEntity> findAllByActiveTrueOrderByIssuedAtDesc(Pageable pageable);

	List<CertificateEntity> findAllByFeaturedTrueAndActiveTrueOrderByIssuedAtDesc();

	long countByFeaturedTrueAndActiveTrue();

	Optional<CertificateEntity> findByIdAndActiveTrue(UUID id);

	void deleteByPatientId(UUID patientId);
}
