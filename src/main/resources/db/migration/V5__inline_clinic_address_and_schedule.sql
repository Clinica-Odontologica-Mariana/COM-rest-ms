ALTER TABLE clinic
    ADD COLUMN IF NOT EXISTS street VARCHAR(150),
    ADD COLUMN IF NOT EXISTS number VARCHAR(20),
    ADD COLUMN IF NOT EXISTS complement VARCHAR(100),
    ADD COLUMN IF NOT EXISTS neighborhood VARCHAR(100),
    ADD COLUMN IF NOT EXISTS city VARCHAR(100),
    ADD COLUMN IF NOT EXISTS state VARCHAR(2),
    ADD COLUMN IF NOT EXISTS zip_code VARCHAR(8),
    ADD COLUMN IF NOT EXISTS working_hours_json TEXT NOT NULL DEFAULT '[]';

UPDATE clinic c
SET street = a.street,
    number = a.number,
    complement = a.complement,
    neighborhood = a.neighborhood,
    city = a.city,
    state = a.state,
    zip_code = a.zip_code
FROM address a
WHERE c.address_id = a.id
  AND (c.street IS NULL
    OR c.city IS NULL
    OR c.state IS NULL
    OR c.zip_code IS NULL);

UPDATE clinic
SET working_hours_json = '[]'
WHERE working_hours_json IS NULL
   OR btrim(working_hours_json) = '';

ALTER TABLE clinic
    DROP CONSTRAINT IF EXISTS fk_clinic_address;

ALTER TABLE clinic
    DROP COLUMN IF EXISTS address_id;

DROP TABLE IF EXISTS clinic_address;
DROP TABLE IF EXISTS working_hours;
