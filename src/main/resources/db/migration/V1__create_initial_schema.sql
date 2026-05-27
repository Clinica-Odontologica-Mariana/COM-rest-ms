CREATE
EXTENSION IF NOT EXISTS "pgcrypto";
CREATE
EXTENSION IF NOT EXISTS "btree_gist";
CREATE
EXTENSION IF NOT EXISTS "citext";

-- Simple schema aligned with the current backend and the planned frontend screens.
-- Public pages: clinic, clinic_address
-- Admin: app_user, professional, specialty, profile photos
-- Patients: patient, patient_address, patient_clinic, medical_record, notes, attachments, odontogram
-- Agenda: appointment, workplace, calendar sync
-- Procedures/treatment: clinical_procedure, treatment_plan, treatment_plan_item
-- Materials/equipment/stock: inventory_item, stock_movement
-- Certificates: certificate

CREATE TABLE app_user
(
    id                UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    keycloak_subject  VARCHAR(100) NOT NULL UNIQUE,
    keycloak_username VARCHAR(150),
    full_name         VARCHAR(150) NOT NULL,
    email             CITEXT       NOT NULL UNIQUE,
    email_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at     TIMESTAMPTZ,
    inactivated_at    TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE address
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    street       VARCHAR(150) NOT NULL,
    number       VARCHAR(20),
    complement   VARCHAR(100),
    neighborhood VARCHAR(100),
    city         VARCHAR(100) NOT NULL,
    state        VARCHAR(2)   NOT NULL,
    zip_code     VARCHAR(8)   NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_address_state CHECK (state ~ '^[A-Z]{2}$'
) ,
    CONSTRAINT chk_address_zip_code CHECK (zip_code ~ '^[0-9]{8}$')
);

CREATE TABLE clinic
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    address_id     UUID,
    name           VARCHAR(150) NOT NULL,
    document       VARCHAR(14)  NOT NULL UNIQUE,
    phone          VARCHAR(20)  NOT NULL,
    email          CITEXT,
    timezone       VARCHAR(80)  NOT NULL DEFAULT 'America/Sao_Paulo',
    description    TEXT,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_clinic_address FOREIGN KEY (address_id) REFERENCES address (id) ON DELETE SET NULL
);

CREATE TABLE clinic_address
(
    clinic_id       UUID        NOT NULL,
    address_id      UUID        NOT NULL,
    label           VARCHAR(80),
    primary_address BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (clinic_id, address_id),
    CONSTRAINT fk_clinic_address_clinic FOREIGN KEY (clinic_id) REFERENCES clinic (id) ON DELETE CASCADE,
    CONSTRAINT fk_clinic_address_address FOREIGN KEY (address_id) REFERENCES address (id) ON DELETE RESTRICT
);

CREATE TABLE workplace
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    clinic_id      UUID         NOT NULL,
    name           VARCHAR(100) NOT NULL,
    description    TEXT,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_workplace_name_per_clinic UNIQUE (clinic_id, name),
    CONSTRAINT uq_workplace_id_clinic UNIQUE (id, clinic_id),
    CONSTRAINT fk_workplace_clinic FOREIGN KEY (clinic_id) REFERENCES clinic (id) ON DELETE RESTRICT
);

CREATE TABLE specialty
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE professional
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id        UUID        NOT NULL UNIQUE,
    -- Primary clinic kept for the current API shape; memberships live in professional_clinic.
    clinic_id      UUID        NOT NULL,
    specialty_id   UUID        NOT NULL,
    license_number VARCHAR(50) NOT NULL,
    bio            TEXT,
    active         BOOLEAN     NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_professional_license UNIQUE (clinic_id, license_number),
    CONSTRAINT uq_professional_id_clinic UNIQUE (id, clinic_id),
    CONSTRAINT fk_professional_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE RESTRICT,
    CONSTRAINT fk_professional_clinic FOREIGN KEY (clinic_id) REFERENCES clinic (id) ON DELETE RESTRICT,
    CONSTRAINT fk_professional_specialty FOREIGN KEY (specialty_id) REFERENCES specialty (id) ON DELETE RESTRICT
);

CREATE TABLE professional_clinic
(
    professional_id UUID        NOT NULL,
    clinic_id       UUID        NOT NULL,
    primary_clinic  BOOLEAN     NOT NULL DEFAULT FALSE,
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (professional_id, clinic_id),
    CONSTRAINT fk_professional_clinic_professional
        FOREIGN KEY (professional_id) REFERENCES professional (id) ON DELETE CASCADE,
    CONSTRAINT fk_professional_clinic_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic (id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX uq_professional_primary_clinic
    ON professional_clinic (professional_id) WHERE primary_clinic = TRUE;

CREATE TABLE professional_address
(
    professional_id UUID        NOT NULL,
    address_id      UUID        NOT NULL,
    label           VARCHAR(80),
    primary_address BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (professional_id, address_id),
    CONSTRAINT fk_professional_address_professional
        FOREIGN KEY (professional_id) REFERENCES professional (id) ON DELETE CASCADE,
    CONSTRAINT fk_professional_address_address
        FOREIGN KEY (address_id) REFERENCES address (id) ON DELETE RESTRICT
);

CREATE TABLE patient
(
    id                      UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    address_id              UUID,
    created_by_user_id      UUID,
    full_name               VARCHAR(150) NOT NULL,
    cpf                     VARCHAR(11)  NOT NULL UNIQUE,
    phone                   VARCHAR(20)  NOT NULL,
    email                   CITEXT,
    birth_date              DATE         NOT NULL,
    emergency_contact_name  VARCHAR(150),
    emergency_contact_phone VARCHAR(20),
    notes                   TEXT,
    active                  BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at          TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_patient_id_cpf UNIQUE (id, cpf),
    CONSTRAINT chk_patient_birth_date CHECK (birth_date <= CURRENT_DATE),
    CONSTRAINT fk_patient_address FOREIGN KEY (address_id) REFERENCES address (id) ON DELETE SET NULL,
    CONSTRAINT fk_patient_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE patient_clinic
(
    patient_id     UUID        NOT NULL,
    clinic_id      UUID        NOT NULL,
    primary_clinic BOOLEAN     NOT NULL DEFAULT FALSE,
    active         BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (patient_id, clinic_id),
    CONSTRAINT fk_patient_clinic_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE CASCADE,
    CONSTRAINT fk_patient_clinic_clinic FOREIGN KEY (clinic_id) REFERENCES clinic (id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX uq_patient_primary_clinic
    ON patient_clinic (patient_id) WHERE primary_clinic = TRUE;

CREATE TABLE patient_address
(
    patient_id      UUID        NOT NULL,
    address_id      UUID        NOT NULL,
    label           VARCHAR(80),
    primary_address BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (patient_id, address_id),
    CONSTRAINT fk_patient_address_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE CASCADE,
    CONSTRAINT fk_patient_address_address FOREIGN KEY (address_id) REFERENCES address (id) ON DELETE RESTRICT
);

CREATE TABLE medical_record
(
    id                     UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    patient_id             UUID        NOT NULL UNIQUE,
    created_by_user_id     UUID,
    allergies              TEXT,
    chronic_conditions     TEXT,
    continuous_medications TEXT,
    general_observations   TEXT,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_medical_record_id_patient UNIQUE (id, patient_id),
    CONSTRAINT fk_medical_record_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE RESTRICT,
    CONSTRAINT fk_medical_record_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE medical_record_note
(
    id                 UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    medical_record_id  UUID        NOT NULL,
    created_by_user_id UUID,
    note               TEXT        NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mrn_record FOREIGN KEY (medical_record_id) REFERENCES medical_record (id) ON DELETE CASCADE,
    CONSTRAINT fk_mrn_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE stored_file
(
    id                  UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    bucket_name         VARCHAR(100) NOT NULL,
    object_key          TEXT         NOT NULL,
    original_file_name  VARCHAR(255) NOT NULL,
    mime_type           VARCHAR(120) NOT NULL DEFAULT 'application/octet-stream',
    size_bytes          BIGINT       NOT NULL DEFAULT 0,
    checksum_sha256     VARCHAR(64),
    etag                VARCHAR(120),
    file_category       VARCHAR(50)  NOT NULL,
    uploaded_by_user_id UUID,
    description         TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_stored_file_bucket_key UNIQUE (bucket_name, object_key),
    CONSTRAINT chk_stored_file_category CHECK (
        file_category IN ('ODONTOGRAM', 'USER_PROFILE_PHOTO', 'MEDICAL_RECORD_ATTACHMENT', 'CERTIFICATE')
    ),
    CONSTRAINT chk_stored_file_size CHECK (size_bytes >= 0),
    CONSTRAINT fk_stored_file_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE medical_record_attachment
(
    id                UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    medical_record_id UUID        NOT NULL,
    stored_file_id    UUID        NOT NULL UNIQUE,
    description       TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mra_record FOREIGN KEY (medical_record_id) REFERENCES medical_record (id) ON DELETE CASCADE,
    CONSTRAINT fk_mra_file FOREIGN KEY (stored_file_id) REFERENCES stored_file (id) ON DELETE RESTRICT
);

CREATE TABLE user_profile_photo
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id        UUID        NOT NULL UNIQUE,
    stored_file_id UUID        NOT NULL UNIQUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profile_photo_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_profile_photo_file FOREIGN KEY (stored_file_id) REFERENCES stored_file (id) ON DELETE RESTRICT
);

CREATE TABLE odontogram_entry
(
    id                          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    medical_record_id           UUID        NOT NULL,
    patient_id                  UUID        NOT NULL,
    tooth_number                INT         NOT NULL,
    surface_code                VARCHAR(20),
    condition_code              VARCHAR(50) NOT NULL,
    notes                       TEXT,
    recorded_by_professional_id UUID,
    recorded_at                 TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_odontogram_record_patient
        FOREIGN KEY (medical_record_id, patient_id) REFERENCES medical_record (id, patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_odontogram_professional
        FOREIGN KEY (recorded_by_professional_id) REFERENCES professional (id) ON DELETE SET NULL
);

CREATE TABLE odontogram_file
(
    id                  UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    patient_id          UUID        NOT NULL,
    medical_record_id   UUID,
    odontogram_entry_id UUID,
    stored_file_id      UUID        NOT NULL UNIQUE,
    description         TEXT,
    created_by_user_id  UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_odontogram_file_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE RESTRICT,
    CONSTRAINT fk_odontogram_file_medical_record FOREIGN KEY (medical_record_id) REFERENCES medical_record (id) ON DELETE SET NULL,
    CONSTRAINT fk_odontogram_file_entry FOREIGN KEY (odontogram_entry_id) REFERENCES odontogram_entry (id) ON DELETE SET NULL,
    CONSTRAINT fk_odontogram_file_stored_file FOREIGN KEY (stored_file_id) REFERENCES stored_file (id) ON DELETE RESTRICT,
    CONSTRAINT fk_odontogram_file_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE appointment_status
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    code            VARCHAR(30) NOT NULL UNIQUE,
    name            VARCHAR(50) NOT NULL UNIQUE,
    blocks_schedule BOOLEAN     NOT NULL DEFAULT TRUE,
    final_status    BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE TABLE calendar_provider
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE calendar_sync_status
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE appointment
(
    id                         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    patient_id                 UUID        NOT NULL,
    clinic_id                  UUID        NOT NULL,
    workplace_id               UUID,
    professional_id            UUID        NOT NULL,
    status_id                  UUID        NOT NULL,
    calendar_provider_id       UUID,
    calendar_sync_status_id    UUID,
    external_calendar_event_id VARCHAR(255),
    last_synced_at             TIMESTAMPTZ,
    blocks_schedule            BOOLEAN     NOT NULL DEFAULT TRUE,
    start_datetime             TIMESTAMPTZ NOT NULL,
    end_datetime               TIMESTAMPTZ NOT NULL,
    notes                      TEXT,
    cancellation_reason        TEXT,
    cancelled_at               TIMESTAMPTZ,
    cancelled_by_user_id       UUID,
    created_by_user_id         UUID,
    created_at                 TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_appointment_id_patient UNIQUE (id, patient_id),
    CONSTRAINT chk_appointment_period CHECK (end_datetime > start_datetime),
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_patient_clinic
        FOREIGN KEY (patient_id, clinic_id) REFERENCES patient_clinic (patient_id, clinic_id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_clinic FOREIGN KEY (clinic_id) REFERENCES clinic (id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_workplace FOREIGN KEY (workplace_id, clinic_id) REFERENCES workplace (id, clinic_id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_professional_clinic
        FOREIGN KEY (professional_id, clinic_id) REFERENCES professional_clinic (professional_id, clinic_id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_status FOREIGN KEY (status_id) REFERENCES appointment_status (id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_calendar_provider FOREIGN KEY (calendar_provider_id) REFERENCES calendar_provider (id) ON DELETE SET NULL,
    CONSTRAINT fk_appointment_calendar_sync_status FOREIGN KEY (calendar_sync_status_id) REFERENCES calendar_sync_status (id) ON DELETE SET NULL,
    CONSTRAINT fk_appointment_cancelled_by FOREIGN KEY (cancelled_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_appointment_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL
);

ALTER TABLE appointment
    ADD CONSTRAINT ex_appointment_professional_overlap EXCLUDE USING gist (
        clinic_id WITH =,
        professional_id WITH =,
        tstzrange(start_datetime, end_datetime, '[)') WITH &&
    )
    WHERE (blocks_schedule = TRUE AND cancelled_at IS NULL);

ALTER TABLE appointment
    ADD CONSTRAINT ex_appointment_workplace_overlap EXCLUDE USING gist (
        clinic_id WITH =,
        workplace_id WITH =,
        tstzrange(start_datetime, end_datetime, '[)') WITH &&
    )
    WHERE (blocks_schedule = TRUE AND cancelled_at IS NULL AND workplace_id IS NOT NULL);

CREATE TABLE clinical_procedure
(
    id                         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    code                       VARCHAR(50) UNIQUE,
    name                       VARCHAR(150) NOT NULL UNIQUE,
    category                   VARCHAR(80),
    description                TEXT,
    estimated_duration_minutes INT,
    base_price                 NUMERIC(10, 2),
    active                     BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at             TIMESTAMPTZ,
    created_at                 TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_clinical_procedure_duration CHECK (estimated_duration_minutes IS NULL OR estimated_duration_minutes > 0),
    CONSTRAINT chk_clinical_procedure_price CHECK (base_price IS NULL OR base_price >= 0)
);

CREATE TABLE treatment_plan
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    patient_id         UUID         NOT NULL,
    medical_record_id  UUID         NOT NULL,
    professional_id    UUID,
    title              VARCHAR(150) NOT NULL,
    status             VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    notes              TEXT,
    total_amount       NUMERIC(10, 2),
    created_by_user_id UUID,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_treatment_plan_status CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_treatment_plan_total CHECK (total_amount IS NULL OR total_amount >= 0),
    CONSTRAINT fk_treatment_plan_record_patient
        FOREIGN KEY (medical_record_id, patient_id) REFERENCES medical_record (id, patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_treatment_plan_professional FOREIGN KEY (professional_id) REFERENCES professional (id) ON DELETE SET NULL,
    CONSTRAINT fk_treatment_plan_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE treatment_plan_item
(
    id                UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    treatment_plan_id UUID        NOT NULL,
    procedure_id      UUID,
    tooth_number      INT,
    description       TEXT        NOT NULL,
    estimated_price   NUMERIC(10, 2),
    status            VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    sort_order        INT         NOT NULL DEFAULT 1,
    completed_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_treatment_plan_item_status CHECK (status IN ('PENDING', 'APPROVED', 'DONE', 'CANCELLED')),
    CONSTRAINT chk_treatment_plan_item_price CHECK (estimated_price IS NULL OR estimated_price >= 0),
    CONSTRAINT chk_treatment_plan_item_order CHECK (sort_order > 0),
    CONSTRAINT fk_treatment_plan_item_plan FOREIGN KEY (treatment_plan_id) REFERENCES treatment_plan (id) ON DELETE CASCADE,
    CONSTRAINT fk_treatment_plan_item_procedure FOREIGN KEY (procedure_id) REFERENCES clinical_procedure (id) ON DELETE SET NULL
);

CREATE TABLE inventory_item
(
    id               UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    clinic_id        UUID           NOT NULL,
    item_type        VARCHAR(30)    NOT NULL,
    name             VARCHAR(150)   NOT NULL,
    description      TEXT,
    sku              VARCHAR(80),
    unit             VARCHAR(30),
    current_quantity NUMERIC(12, 2) NOT NULL DEFAULT 0,
    minimum_quantity NUMERIC(12, 2),
    active           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_inventory_item_type CHECK (item_type IN ('MATERIAL', 'EQUIPMENT')),
    CONSTRAINT chk_inventory_quantities CHECK (
        current_quantity >= 0 AND (minimum_quantity IS NULL OR minimum_quantity >= 0)
        ),
    CONSTRAINT uq_inventory_item_name_per_clinic UNIQUE (clinic_id, name),
    CONSTRAINT fk_inventory_item_clinic FOREIGN KEY (clinic_id) REFERENCES clinic (id) ON DELETE RESTRICT
);

CREATE TABLE stock_movement
(
    id                 UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    inventory_item_id  UUID           NOT NULL,
    movement_type      VARCHAR(20)    NOT NULL,
    quantity           NUMERIC(12, 2) NOT NULL,
    reason             VARCHAR(255),
    created_by_user_id UUID,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_stock_movement_type CHECK (movement_type IN ('IN', 'OUT', 'ADJUSTMENT')),
    CONSTRAINT chk_stock_movement_quantity CHECK (quantity > 0),
    CONSTRAINT fk_stock_movement_item FOREIGN KEY (inventory_item_id) REFERENCES inventory_item (id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_movement_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE certificate
(
    id               UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    patient_id       UUID         NOT NULL,
    professional_id  UUID,
    title            VARCHAR(150) NOT NULL,
    certificate_type VARCHAR(50)  NOT NULL DEFAULT 'ATTENDANCE',
    content          TEXT,
    issued_at        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    stored_file_id   UUID,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    revoked_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_certificate_patient FOREIGN KEY (patient_id) REFERENCES patient (id) ON DELETE RESTRICT,
    CONSTRAINT fk_certificate_professional FOREIGN KEY (professional_id) REFERENCES professional (id) ON DELETE SET NULL,
    CONSTRAINT fk_certificate_file FOREIGN KEY (stored_file_id) REFERENCES stored_file (id) ON DELETE SET NULL
);

CREATE
OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at
= CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$;

CREATE
OR REPLACE FUNCTION fn_sync_professional_primary_clinic()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
UPDATE professional_clinic
SET primary_clinic = FALSE
WHERE professional_id = NEW.id
  AND primary_clinic = TRUE
  AND clinic_id <> NEW.clinic_id;

INSERT INTO professional_clinic (professional_id, clinic_id, primary_clinic, active)
VALUES (NEW.id, NEW.clinic_id, TRUE, NEW.active) ON CONFLICT (professional_id, clinic_id)
    DO
UPDATE SET primary_clinic = TRUE,
    active = EXCLUDED.active;

RETURN NEW;
END;
$$;

CREATE TRIGGER trg_app_user_updated_at
    BEFORE UPDATE
    ON app_user
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_address_updated_at
    BEFORE UPDATE
    ON address
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_clinic_updated_at
    BEFORE UPDATE
    ON clinic
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_workplace_updated_at
    BEFORE UPDATE
    ON workplace
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_professional_updated_at
    BEFORE UPDATE
    ON professional
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_professional_sync_primary_clinic
    AFTER INSERT OR
UPDATE OF clinic_id, active
ON professional
    FOR EACH ROW EXECUTE FUNCTION fn_sync_professional_primary_clinic();
CREATE TRIGGER trg_patient_updated_at
    BEFORE UPDATE
    ON patient
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_medical_record_updated_at
    BEFORE UPDATE
    ON medical_record
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_appointment_updated_at
    BEFORE UPDATE
    ON appointment
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_clinical_procedure_updated_at
    BEFORE UPDATE
    ON clinical_procedure
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_treatment_plan_updated_at
    BEFORE UPDATE
    ON treatment_plan
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_inventory_item_updated_at
    BEFORE UPDATE
    ON inventory_item
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_certificate_updated_at
    BEFORE UPDATE
    ON certificate
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE INDEX idx_app_user_email ON app_user (email);
CREATE INDEX idx_patient_name ON patient (full_name);
CREATE INDEX idx_patient_active ON patient (active);
CREATE INDEX idx_medical_record_patient ON medical_record (patient_id);
CREATE INDEX idx_stored_file_category ON stored_file (file_category);
CREATE INDEX idx_odontogram_patient ON odontogram_entry (patient_id);
CREATE INDEX idx_odontogram_file_patient ON odontogram_file (patient_id);
CREATE INDEX idx_professional_clinic ON professional (clinic_id);
CREATE INDEX idx_workplace_clinic ON workplace (clinic_id);
CREATE INDEX idx_appointment_patient ON appointment (patient_id);
CREATE INDEX idx_appointment_clinic ON appointment (clinic_id);
CREATE INDEX idx_appointment_professional ON appointment (professional_id);
CREATE INDEX idx_appointment_start ON appointment (start_datetime);
CREATE INDEX idx_clinical_procedure_active ON clinical_procedure (active);
CREATE INDEX idx_treatment_plan_patient ON treatment_plan (patient_id);
CREATE INDEX idx_inventory_item_clinic ON inventory_item (clinic_id);
CREATE INDEX idx_stock_movement_item ON stock_movement (inventory_item_id);
CREATE INDEX idx_certificate_patient ON certificate (patient_id);
INSERT INTO appointment_status (code, name, blocks_schedule, final_status)
VALUES ('SCHEDULED', 'Agendado', TRUE, FALSE),
       ('CONFIRMED', 'Confirmado', TRUE, FALSE),
       ('IN_PROGRESS', 'Em atendimento', TRUE, FALSE),
       ('COMPLETED', 'Concluido', FALSE, TRUE),
       ('CANCELLED', 'Cancelado', FALSE, TRUE),
       ('NO_SHOW', 'Nao compareceu', FALSE, TRUE);

INSERT INTO calendar_provider (code, name)
VALUES ('GOOGLE', 'Google Calendar');

INSERT INTO calendar_sync_status (code, name)
VALUES ('PENDING', 'Pendente'),
       ('SYNCED', 'Sincronizado'),
       ('FAILED', 'Falhou'),
       ('NOT_SYNCED', 'Nao sincronizado');

INSERT INTO specialty (code, name)
VALUES ('DENTISTRY', 'Odontologia'),
       ('ORTHODONTICS', 'Ortodontia'),
       ('IMPLANTODONTICS', 'Implantodontia'),
       ('PEDIATRIC_DENTISTRY', 'Odontopediatria'),
       ('ENDODONTICS', 'Endodontia');
