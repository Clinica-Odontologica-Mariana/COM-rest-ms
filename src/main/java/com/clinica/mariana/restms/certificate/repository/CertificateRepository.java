package com.clinica.mariana.restms.certificate.repository;

import com.clinica.mariana.restms.certificate.entity.CertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<CertificateEntity, UUID> {

	List<CertificateEntity> findAllByActiveTrueOrderByIssuedAtDesc();

	List<CertificateEntity> findAllByPatientIdAndActiveTrueOrderByIssuedAtDesc(UUID patientId);
}
