ALTER TABLE appointment
    ADD COLUMN procedure_id UUID,
    ADD CONSTRAINT fk_appointment_procedure FOREIGN KEY (procedure_id) REFERENCES clinical_procedure (id) ON DELETE SET NULL;
