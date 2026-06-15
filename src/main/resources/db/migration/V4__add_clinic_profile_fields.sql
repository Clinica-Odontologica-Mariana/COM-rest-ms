ALTER TABLE clinic
    DROP COLUMN IF EXISTS document;

ALTER TABLE clinic
    ADD COLUMN IF NOT EXISTS whatsapp VARCHAR(20),
    ADD COLUMN IF NOT EXISTS instagram VARCHAR(80),
    ADD COLUMN IF NOT EXISTS clinic_photo_file_id UUID,
    ADD COLUMN IF NOT EXISTS inactive_type VARCHAR(20),
    ADD COLUMN IF NOT EXISTS inactive_from DATE,
    ADD COLUMN IF NOT EXISTS inactive_to DATE;

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_clinic_photo_file'
    ) THEN
        ALTER TABLE clinic
            ADD CONSTRAINT fk_clinic_photo_file
            FOREIGN KEY (clinic_photo_file_id) REFERENCES stored_file (id) ON DELETE SET NULL;
    END IF;
END;
$$;

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_clinic_inactive_type'
    ) THEN
        ALTER TABLE clinic
            ADD CONSTRAINT chk_clinic_inactive_type
            CHECK (inactive_type IS NULL OR inactive_type IN ('permanent', 'temporary'));
    END IF;
END;
$$;

ALTER TABLE stored_file
    DROP CONSTRAINT IF EXISTS chk_stored_file_category;

ALTER TABLE stored_file
    ADD CONSTRAINT chk_stored_file_category
    CHECK (
        file_category IN (
            'ODONTOGRAM',
            'USER_PROFILE_PHOTO',
            'MEDICAL_RECORD_ATTACHMENT',
            'CERTIFICATE',
            'LEGACY',
            'CLINIC_PHOTO'
        )
    );
