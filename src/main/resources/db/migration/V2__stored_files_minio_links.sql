ALTER TABLE stored_file
    DROP CONSTRAINT IF EXISTS chk_stored_file_category;

ALTER TABLE stored_file
    ADD COLUMN IF NOT EXISTS file_category VARCHAR(50),
    ADD COLUMN IF NOT EXISTS description TEXT;

UPDATE stored_file
SET file_category = 'LEGACY'
WHERE file_category IS NULL;

UPDATE stored_file
SET mime_type = 'application/octet-stream'
WHERE mime_type IS NULL;

UPDATE stored_file
SET size_bytes = 0
WHERE size_bytes IS NULL;

ALTER TABLE stored_file
    ALTER COLUMN file_category SET NOT NULL,
    ALTER COLUMN mime_type SET NOT NULL,
    ALTER COLUMN size_bytes SET NOT NULL;

ALTER TABLE stored_file
    ADD CONSTRAINT chk_stored_file_category
        CHECK (
            file_category IN (
                'ODONTOGRAM',
                'USER_PROFILE_PHOTO',
                'MEDICAL_RECORD_ATTACHMENT',
                'CERTIFICATE',
                'LEGACY'
            )
        );

CREATE TABLE IF NOT EXISTS odontogram_file
(
    id                  UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    patient_id          UUID        NOT NULL,
    medical_record_id   UUID,
    odontogram_entry_id UUID,
    stored_file_id      UUID        NOT NULL,
    description         TEXT,
    created_by_user_id  UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_odontogram_file_stored_file UNIQUE (stored_file_id),
    CONSTRAINT fk_odontogram_file_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_odontogram_file_medical_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_record (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_odontogram_file_entry
        FOREIGN KEY (odontogram_entry_id) REFERENCES odontogram_entry (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_odontogram_file_stored_file
        FOREIGN KEY (stored_file_id) REFERENCES stored_file (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_odontogram_file_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id)
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_profile_photo
(
    id                 UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id            UUID        NOT NULL,
    stored_file_id     UUID        NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_profile_photo_user UNIQUE (user_id),
    CONSTRAINT uq_user_profile_photo_file UNIQUE (stored_file_id),
    CONSTRAINT fk_user_profile_photo_user
        FOREIGN KEY (user_id) REFERENCES app_user (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_profile_photo_file
        FOREIGN KEY (stored_file_id) REFERENCES stored_file (id)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_stored_file_category ON stored_file (file_category);
CREATE INDEX IF NOT EXISTS idx_stored_file_uploaded_by ON stored_file (uploaded_by_user_id);
CREATE INDEX IF NOT EXISTS idx_odontogram_file_patient ON odontogram_file (patient_id);
CREATE INDEX IF NOT EXISTS idx_odontogram_file_record ON odontogram_file (medical_record_id);
CREATE INDEX IF NOT EXISTS idx_odontogram_file_entry ON odontogram_file (odontogram_entry_id);
CREATE INDEX IF NOT EXISTS idx_user_profile_photo_user ON user_profile_photo (user_id);
