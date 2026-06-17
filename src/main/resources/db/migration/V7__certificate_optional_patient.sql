-- Certificates are credentials of the clinic/professional, not patient documents.
-- Make patient_id optional so a certificate can be created without a patient.
-- The FK (fk_certificate_patient) still applies when patient_id is provided.
ALTER TABLE certificate
    ALTER COLUMN patient_id DROP NOT NULL;
