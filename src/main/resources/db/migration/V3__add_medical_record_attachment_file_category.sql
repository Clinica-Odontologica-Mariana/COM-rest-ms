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
                'LEGACY'
            )
        );
