CREATE
EXTENSION IF NOT EXISTS "pgcrypto";
CREATE
EXTENSION IF NOT EXISTS "btree_gist";
CREATE
EXTENSION IF NOT EXISTS "citext";

-- ============================================================
-- CLINICA ODONTOLOGICA MARIANA - FINAL DATABASE SCHEMA
-- PostgreSQL
--
-- Arquitetura considerada:
-- - SSO/autenticacao pelo Keycloak
-- - Armazenamento de arquivos/imagens no MinIO
--
-- Caracteristicas:
-- - Sem senha local no banco da aplicacao
-- - app_user referencia o subject/ID do usuario no Keycloak
-- - Arquivos modelados como objetos MinIO/S3
-- - Agenda com protecao contra conflitos via EXCLUDE USING gist
-- - TIMESTAMPTZ para eventos temporais
-- - Enderecos estruturados
-- - Prontuario, evolucao clinica, odontograma e plano de tratamento
-- - Financeiro com cobranca e pagamentos separados
-- - Convenios
-- - Consentimentos LGPD
-- - Auditoria basica com ator vindo de app.current_user_id
--
-- Observacao:
-- Este script e apropriado para bootstrap/dev. Para producao, converta
-- em migrations versionadas e remova o bloco DROP.
-- ============================================================

-- ============================================================
-- DOMAIN TABLES
-- ============================================================

CREATE TABLE role
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE appointment_status
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    code            VARCHAR(30) NOT NULL UNIQUE,
    name            VARCHAR(50) NOT NULL UNIQUE,
    blocks_schedule BOOLEAN     NOT NULL DEFAULT TRUE,
    final_status    BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE TABLE clinical_visit_status
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE treatment_plan_status
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tooth_condition
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE tooth_surface
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL UNIQUE
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

CREATE TABLE payment_status
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE invoice_status
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE payment_method
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE consent_type
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(120) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE blog_post_status
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE service_category
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE service_cost_type
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE social_platform
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE specialty
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- ============================================================
-- SECURITY / USERS - KEYCLOAK SSO
-- ============================================================

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
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_app_user_email_format
        CHECK (email::TEXT ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'
) ,
    CONSTRAINT chk_app_user_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
        )
);

CREATE TABLE user_role
(
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id) REFERENCES app_user (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES role (id)
            ON DELETE RESTRICT
);

-- ============================================================
-- CORE ENTITIES
-- ============================================================

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
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_clinic_document_format CHECK (document ~ '^[0-9]{14}$'
) ,
    CONSTRAINT chk_clinic_email_format
        CHECK (email IS NULL OR email::TEXT ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'),
    CONSTRAINT chk_clinic_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
        ),
    CONSTRAINT fk_clinic_address
        FOREIGN KEY (address_id) REFERENCES address (id)
        ON DELETE SET NULL
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
    CONSTRAINT chk_workplace_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
            ),
    CONSTRAINT fk_workplace_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic (id)
            ON DELETE RESTRICT
);

CREATE TABLE professional
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id        UUID        NOT NULL UNIQUE,
    clinic_id      UUID        NOT NULL,
    specialty_id   UUID        NOT NULL,
    license_number VARCHAR(50) NOT NULL,
    active         BOOLEAN     NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_professional_license UNIQUE (clinic_id, license_number),
    CONSTRAINT uq_professional_id_clinic UNIQUE (id, clinic_id),
    CONSTRAINT chk_professional_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
            ),
    CONSTRAINT fk_professional_user
        FOREIGN KEY (user_id) REFERENCES app_user (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_professional_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_professional_specialty
        FOREIGN KEY (specialty_id) REFERENCES specialty (id)
            ON DELETE RESTRICT
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
    CONSTRAINT chk_patient_cpf_format CHECK (cpf ~ '^[0-9]{11}$'
) ,
    CONSTRAINT chk_patient_email_format
        CHECK (email IS NULL OR email::TEXT ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'),
    CONSTRAINT chk_patient_birth_date CHECK (birth_date <= CURRENT_DATE),
    CONSTRAINT chk_patient_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
        ),
    CONSTRAINT fk_patient_address
        FOREIGN KEY (address_id) REFERENCES address (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_patient_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id)
        ON DELETE SET NULL
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
    CONSTRAINT fk_medical_record_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_medical_record_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id)
            ON DELETE SET NULL
);

CREATE TABLE medical_record_note
(
    id                 UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    medical_record_id  UUID        NOT NULL,
    created_by_user_id UUID,
    note               TEXT        NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mrn_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_record (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_mrn_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id)
            ON DELETE SET NULL
);

-- ============================================================
-- FILE STORAGE - MINIO
-- ============================================================

CREATE TABLE stored_file
(
    id                  UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    bucket_name         VARCHAR(100) NOT NULL,
    object_key          TEXT         NOT NULL,
    original_file_name  VARCHAR(255) NOT NULL,
    mime_type           VARCHAR(120),
    size_bytes          BIGINT,
    checksum_sha256     VARCHAR(64),
    etag                VARCHAR(120),
    uploaded_by_user_id UUID,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_stored_file_bucket_key UNIQUE (bucket_name, object_key),
    CONSTRAINT chk_stored_file_bucket CHECK (bucket_name ~ '^[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$'
) ,
    CONSTRAINT chk_stored_file_size CHECK (size_bytes IS NULL OR size_bytes >= 0),
    CONSTRAINT chk_stored_file_checksum CHECK (checksum_sha256 IS NULL OR checksum_sha256 ~ '^[a-fA-F0-9]{64}$'),
    CONSTRAINT fk_stored_file_uploaded_by
        FOREIGN KEY (uploaded_by_user_id) REFERENCES app_user (id)
        ON DELETE SET NULL
);

CREATE TABLE medical_record_attachment
(
    id                UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    medical_record_id UUID        NOT NULL,
    stored_file_id    UUID        NOT NULL,
    description       TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_mra_file UNIQUE (stored_file_id),
    CONSTRAINT fk_mra_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_record (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_mra_file
        FOREIGN KEY (stored_file_id) REFERENCES stored_file (id)
            ON DELETE RESTRICT
);

-- ============================================================
-- SERVICES / PRICING / COSTS
-- ============================================================

CREATE TABLE service
(
    id                         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    category_id                UUID         NOT NULL,
    created_by_user_id         UUID,
    name                       VARCHAR(150) NOT NULL,
    description                TEXT,
    estimated_duration_minutes INT,
    active                     BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at             TIMESTAMPTZ,
    created_at                 TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_service_name_per_category UNIQUE (category_id, name),
    CONSTRAINT chk_service_duration CHECK (
        estimated_duration_minutes IS NULL OR estimated_duration_minutes > 0
        ),
    CONSTRAINT chk_service_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
            ),
    CONSTRAINT fk_service_category
        FOREIGN KEY (category_id) REFERENCES service_category (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_service_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id)
            ON DELETE SET NULL
);

CREATE TABLE service_price
(
    id          UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    service_id  UUID           NOT NULL,
    amount      NUMERIC(10, 2) NOT NULL,
    description TEXT,
    valid_from  DATE           NOT NULL DEFAULT CURRENT_DATE,
    valid_to    DATE,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_service_price_amount CHECK (amount >= 0),
    CONSTRAINT chk_service_price_period CHECK (valid_to IS NULL OR valid_to >= valid_from),
    CONSTRAINT fk_price_service
        FOREIGN KEY (service_id) REFERENCES service (id)
            ON DELETE RESTRICT
);

CREATE TABLE service_cost
(
    id           UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    service_id   UUID           NOT NULL,
    cost_type_id UUID           NOT NULL,
    amount       NUMERIC(10, 2) NOT NULL,
    description  TEXT,
    valid_from   DATE           NOT NULL DEFAULT CURRENT_DATE,
    valid_to     DATE,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_service_cost_amount CHECK (amount >= 0),
    CONSTRAINT chk_service_cost_period CHECK (valid_to IS NULL OR valid_to >= valid_from),
    CONSTRAINT fk_cost_service
        FOREIGN KEY (service_id) REFERENCES service (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_cost_type
        FOREIGN KEY (cost_type_id) REFERENCES service_cost_type (id)
            ON DELETE RESTRICT
);

ALTER TABLE service_price
    ADD CONSTRAINT ex_service_price_period EXCLUDE USING gist (
        service_id WITH =,
        daterange(valid_from, COALESCE(valid_to, 'infinity'::DATE), '[]') WITH &&
    );

ALTER TABLE service_cost
    ADD CONSTRAINT ex_service_cost_period EXCLUDE USING gist (
        service_id WITH =,
        cost_type_id WITH =,
        daterange(valid_from, COALESCE(valid_to, 'infinity'::DATE), '[]') WITH &&
    );

-- ============================================================
-- EQUIPMENT / AVAILABILITY
-- ============================================================

CREATE TABLE equipment
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    clinic_id      UUID         NOT NULL,
    name           VARCHAR(150) NOT NULL,
    description    TEXT,
    location       VARCHAR(100),
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_equipment_name_per_clinic UNIQUE (clinic_id, name),
    CONSTRAINT chk_equipment_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
            ),
    CONSTRAINT fk_equipment_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic (id)
            ON DELETE RESTRICT
);

CREATE TABLE working_hours
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinic_id   UUID NOT NULL,
    day_of_week INT  NOT NULL,
    start_time  TIME NOT NULL,
    end_time    TIME NOT NULL,
    CONSTRAINT chk_working_hours_day CHECK (day_of_week BETWEEN 0 AND 6),
    CONSTRAINT chk_working_hours_time CHECK (end_time > start_time),
    CONSTRAINT fk_working_hours_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic (id)
            ON DELETE CASCADE
);

ALTER TABLE working_hours
    ADD CONSTRAINT ex_working_hours_overlap EXCLUDE USING gist (
        clinic_id WITH =,
        day_of_week WITH =,
        tsrange(
            '2000-01-01'::DATE + start_time,
            '2000-01-01'::DATE + end_time,
            '[)'
        ) WITH &&
    );

CREATE TABLE schedule_block
(
    id                 UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    clinic_id          UUID        NOT NULL,
    workplace_id       UUID,
    professional_id    UUID,
    start_datetime     TIMESTAMPTZ NOT NULL,
    end_datetime       TIMESTAMPTZ NOT NULL,
    reason             VARCHAR(255),
    created_by_user_id UUID,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_schedule_block_period CHECK (end_datetime > start_datetime),
    CONSTRAINT fk_block_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_block_workplace
        FOREIGN KEY (workplace_id, clinic_id) REFERENCES workplace (id, clinic_id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_block_professional
        FOREIGN KEY (professional_id, clinic_id) REFERENCES professional (id, clinic_id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_block_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id)
            ON DELETE SET NULL
);

-- ============================================================
-- APPOINTMENTS
-- ============================================================

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
    CONSTRAINT chk_appointment_cancelled CHECK (
        (cancelled_at IS NULL AND cancelled_by_user_id IS NULL) OR
        (cancelled_at IS NOT NULL)
        ),
    CONSTRAINT fk_appointment_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_workplace
        FOREIGN KEY (workplace_id, clinic_id) REFERENCES workplace (id, clinic_id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_professional
        FOREIGN KEY (professional_id, clinic_id) REFERENCES professional (id, clinic_id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_status
        FOREIGN KEY (status_id) REFERENCES appointment_status (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_calendar_provider
        FOREIGN KEY (calendar_provider_id) REFERENCES calendar_provider (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_appointment_calendar_sync_status
        FOREIGN KEY (calendar_sync_status_id) REFERENCES calendar_sync_status (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_appointment_cancelled_by
        FOREIGN KEY (cancelled_by_user_id) REFERENCES app_user (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_appointment_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id)
            ON DELETE SET NULL
);

ALTER TABLE appointment
    ADD CONSTRAINT ex_appointment_professional_overlap EXCLUDE USING gist (
        clinic_id WITH =,
        professional_id WITH =,
        tstzrange(start_datetime, end_datetime, '[)') WITH &&
    )
    WHERE (blocks_schedule = TRUE);

ALTER TABLE appointment
    ADD CONSTRAINT ex_appointment_workplace_overlap EXCLUDE USING gist (
        clinic_id WITH =,
        workplace_id WITH =,
        tstzrange(start_datetime, end_datetime, '[)') WITH &&
    )
    WHERE (blocks_schedule = TRUE AND workplace_id IS NOT NULL);

CREATE TABLE appointment_service
(
    appointment_id UUID NOT NULL,
    service_id     UUID NOT NULL,
    quantity       INT  NOT NULL DEFAULT 1,
    unit_price     NUMERIC(10, 2),
    notes          TEXT,
    PRIMARY KEY (appointment_id, service_id),
    CONSTRAINT chk_appointment_service_quantity CHECK (quantity > 0),
    CONSTRAINT chk_appointment_service_unit_price CHECK (unit_price IS NULL OR unit_price >= 0),
    CONSTRAINT fk_appointment_service_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointment (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_appointment_service_service
        FOREIGN KEY (service_id) REFERENCES service (id)
            ON DELETE RESTRICT
);

-- ============================================================
-- CLINICAL VISITS / RECORD EVOLUTION
-- ============================================================

CREATE TABLE clinical_visit
(
    id                 UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    appointment_id     UUID UNIQUE,
    patient_id         UUID        NOT NULL,
    professional_id    UUID        NOT NULL,
    medical_record_id  UUID        NOT NULL,
    status_id          UUID        NOT NULL,
    chief_complaint    TEXT,
    diagnosis          TEXT,
    procedure_notes    TEXT,
    prescription       TEXT,
    follow_up_notes    TEXT,
    started_at         TIMESTAMPTZ,
    finished_at        TIMESTAMPTZ,
    created_by_user_id UUID,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_clinical_visit_period
        CHECK (finished_at IS NULL OR started_at IS NULL OR finished_at >= started_at),
    CONSTRAINT fk_visit_appointment
        FOREIGN KEY (appointment_id, patient_id) REFERENCES appointment (id, patient_id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_visit_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_visit_professional
        FOREIGN KEY (professional_id) REFERENCES professional (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_visit_record_patient
        FOREIGN KEY (medical_record_id, patient_id) REFERENCES medical_record (id, patient_id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_visit_status
        FOREIGN KEY (status_id) REFERENCES clinical_visit_status (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_visit_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id)
            ON DELETE SET NULL
);

CREATE TABLE clinical_visit_attachment
(
    id                UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    clinical_visit_id UUID        NOT NULL,
    stored_file_id    UUID        NOT NULL,
    description       TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cva_file UNIQUE (stored_file_id),
    CONSTRAINT fk_cva_visit
        FOREIGN KEY (clinical_visit_id) REFERENCES clinical_visit (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_cva_file
        FOREIGN KEY (stored_file_id) REFERENCES stored_file (id)
            ON DELETE RESTRICT
);

-- ============================================================
-- ODONTOGRAM / TREATMENT PLAN
-- ============================================================

CREATE TABLE odontogram_entry
(
    id                          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    medical_record_id           UUID        NOT NULL,
    patient_id                  UUID        NOT NULL,
    tooth_number                INT         NOT NULL,
    surface_id                  UUID,
    condition_id                UUID        NOT NULL,
    notes                       TEXT,
    recorded_by_professional_id UUID,
    recorded_at                 TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_odontogram_tooth_number CHECK (
        (
            tooth_number BETWEEN 11 AND 48
                AND tooth_number NOT IN (19, 20, 29, 30, 39, 40)
            )
            OR
        (
            tooth_number BETWEEN 51 AND 85
                AND tooth_number NOT IN (59, 60, 69, 70, 79, 80)
            )
        ),
    CONSTRAINT fk_odontogram_record_patient
        FOREIGN KEY (medical_record_id, patient_id) REFERENCES medical_record (id, patient_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_odontogram_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_odontogram_surface
        FOREIGN KEY (surface_id) REFERENCES tooth_surface (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_odontogram_condition
        FOREIGN KEY (condition_id) REFERENCES tooth_condition (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_odontogram_professional
        FOREIGN KEY (recorded_by_professional_id) REFERENCES professional (id)
            ON DELETE SET NULL
);

CREATE TABLE treatment_plan
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    medical_record_id  UUID         NOT NULL,
    patient_id         UUID         NOT NULL,
    professional_id    UUID         NOT NULL,
    status_id          UUID         NOT NULL,
    title              VARCHAR(150) NOT NULL,
    description        TEXT,
    created_by_user_id UUID,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tp_record_patient
        FOREIGN KEY (medical_record_id, patient_id) REFERENCES medical_record (id, patient_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_tp_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_tp_professional
        FOREIGN KEY (professional_id) REFERENCES professional (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_tp_status
        FOREIGN KEY (status_id) REFERENCES treatment_plan_status (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_tp_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id)
            ON DELETE SET NULL
);

CREATE TABLE treatment_plan_item
(
    id                 UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    treatment_plan_id  UUID        NOT NULL,
    service_id         UUID,
    tooth_number       INT,
    surface_id         UUID,
    description        TEXT        NOT NULL,
    estimated_price    NUMERIC(10, 2),
    sequence_order     INT         NOT NULL DEFAULT 1,
    completed_visit_id UUID,
    completed_at       TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_tpi_tooth_number CHECK (
        tooth_number IS NULL OR
        (
            tooth_number BETWEEN 11 AND 48
                AND tooth_number NOT IN (19, 20, 29, 30, 39, 40)
            )
            OR
        (
            tooth_number BETWEEN 51 AND 85
                AND tooth_number NOT IN (59, 60, 69, 70, 79, 80)
            )
        ),
    CONSTRAINT chk_tpi_price CHECK (estimated_price IS NULL OR estimated_price >= 0),
    CONSTRAINT chk_tpi_sequence CHECK (sequence_order > 0),
    CONSTRAINT fk_tpi_plan
        FOREIGN KEY (treatment_plan_id) REFERENCES treatment_plan (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_tpi_service
        FOREIGN KEY (service_id) REFERENCES service (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_tpi_surface
        FOREIGN KEY (surface_id) REFERENCES tooth_surface (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_tpi_visit
        FOREIGN KEY (completed_visit_id) REFERENCES clinical_visit (id)
            ON DELETE SET NULL
);

-- ============================================================
-- INSURANCE / LGPD
-- ============================================================

CREATE TABLE insurance_provider
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name           VARCHAR(150) NOT NULL UNIQUE,
    document       VARCHAR(14),
    phone          VARCHAR(20),
    email          CITEXT,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_insurance_document CHECK (document IS NULL OR document ~ '^[0-9]{14}$'
) ,
    CONSTRAINT chk_insurance_email_format
        CHECK (email IS NULL OR email::TEXT ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'),
    CONSTRAINT chk_insurance_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
        )
);

CREATE TABLE patient_insurance
(
    id                    UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    patient_id            UUID        NOT NULL,
    insurance_provider_id UUID        NOT NULL,
    plan_name             VARCHAR(100),
    card_number           VARCHAR(80),
    authorization_number  VARCHAR(100),
    valid_until           DATE,
    active                BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_patient_insurance_card UNIQUE (insurance_provider_id, card_number),
    CONSTRAINT fk_patient_insurance_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_patient_insurance_provider
        FOREIGN KEY (insurance_provider_id) REFERENCES insurance_provider (id)
            ON DELETE RESTRICT
);

CREATE TABLE patient_consent
(
    id                 UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    patient_id         UUID        NOT NULL,
    consent_type_id    UUID        NOT NULL,
    stored_file_id     UUID,
    granted            BOOLEAN     NOT NULL,
    granted_at         TIMESTAMPTZ,
    revoked_at         TIMESTAMPTZ,
    created_by_user_id UUID,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_patient_consent_dates CHECK (
        revoked_at IS NULL OR granted_at IS NULL OR revoked_at >= granted_at
        ),
    CONSTRAINT chk_patient_consent_granted_at CHECK (
        (granted = TRUE AND granted_at IS NOT NULL) OR
        (granted = FALSE)
        ),
    CONSTRAINT fk_patient_consent_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_patient_consent_type
        FOREIGN KEY (consent_type_id) REFERENCES consent_type (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_patient_consent_file
        FOREIGN KEY (stored_file_id) REFERENCES stored_file (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_patient_consent_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id)
            ON DELETE SET NULL
);

-- ============================================================
-- FINANCIAL - INVOICES AND PAYMENTS
-- ============================================================

CREATE TABLE invoice
(
    id                 UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    patient_id         UUID           NOT NULL,
    appointment_id     UUID,
    status_id          UUID           NOT NULL,
    gross_amount       NUMERIC(10, 2) NOT NULL,
    discount_amount    NUMERIC(10, 2) NOT NULL DEFAULT 0,
    net_amount         NUMERIC(10, 2) NOT NULL,
    due_date           DATE,
    notes              TEXT,
    created_by_user_id UUID,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_invoice_amounts CHECK (
        gross_amount >= 0
            AND discount_amount >= 0
            AND discount_amount <= gross_amount
            AND net_amount = gross_amount - discount_amount
        ),
    CONSTRAINT fk_invoice_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_invoice_appointment_patient
        FOREIGN KEY (appointment_id, patient_id) REFERENCES appointment (id, patient_id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_invoice_status
        FOREIGN KEY (status_id) REFERENCES invoice_status (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_invoice_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id)
            ON DELETE SET NULL
);

CREATE TABLE invoice_item
(
    id           UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    invoice_id   UUID           NOT NULL,
    service_id   UUID,
    description  VARCHAR(255)   NOT NULL,
    quantity     INT            NOT NULL DEFAULT 1,
    unit_amount  NUMERIC(10, 2) NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_invoice_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_invoice_item_amounts CHECK (
        unit_amount >= 0 AND total_amount = quantity * unit_amount
        ),
    CONSTRAINT fk_invoice_item_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoice (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_invoice_item_service
        FOREIGN KEY (service_id) REFERENCES service (id)
            ON DELETE SET NULL
);

CREATE TABLE payment
(
    id                 UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    invoice_id         UUID           NOT NULL,
    patient_id         UUID           NOT NULL,
    status_id          UUID           NOT NULL,
    method_id          UUID,
    amount             NUMERIC(10, 2) NOT NULL,
    paid_at            TIMESTAMPTZ,
    external_reference VARCHAR(150),
    notes              TEXT,
    created_by_user_id UUID,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payment_amount CHECK (amount >= 0),
    CONSTRAINT fk_payment_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoice (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_payment_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_payment_status
        FOREIGN KEY (status_id) REFERENCES payment_status (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_payment_method
        FOREIGN KEY (method_id) REFERENCES payment_method (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_payment_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id)
            ON DELETE SET NULL
);

-- ============================================================
-- CONTENT / SOCIAL
-- ============================================================

CREATE TABLE blog_post
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    status_id      UUID         NOT NULL,
    author_user_id UUID,
    cover_file_id  UUID,
    title          VARCHAR(200) NOT NULL,
    slug           VARCHAR(220) NOT NULL UNIQUE,
    summary        TEXT,
    content        TEXT         NOT NULL,
    published_at   TIMESTAMPTZ,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_blog_slug CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'
) ,
    CONSTRAINT chk_blog_post_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
        ),
    CONSTRAINT fk_blog_status
        FOREIGN KEY (status_id) REFERENCES blog_post_status (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_blog_author
        FOREIGN KEY (author_user_id) REFERENCES app_user (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_blog_cover_file
        FOREIGN KEY (cover_file_id) REFERENCES stored_file (id)
        ON DELETE SET NULL
);

CREATE TABLE social_link
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    clinic_id   UUID        NOT NULL,
    platform_id UUID        NOT NULL,
    url         TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_social_link UNIQUE (clinic_id, platform_id),
    CONSTRAINT chk_social_url CHECK (url ~* '^https?://'
) ,
    CONSTRAINT fk_social_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_social_platform
        FOREIGN KEY (platform_id) REFERENCES social_platform (id)
        ON DELETE RESTRICT
);

-- ============================================================
-- AUDIT
-- ============================================================

CREATE TABLE audit_log
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    table_name    VARCHAR(100) NOT NULL,
    row_id        UUID,
    operation     VARCHAR(10)  NOT NULL,
    actor_user_id UUID,
    old_data      JSONB,
    new_data      JSONB,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_audit_operation CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    CONSTRAINT fk_audit_actor
        FOREIGN KEY (actor_user_id) REFERENCES app_user (id)
            ON DELETE SET NULL
);

-- ============================================================
-- FUNCTIONS
-- ============================================================

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
OR REPLACE FUNCTION fn_validate_appointment()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
v_blocks_schedule BOOLEAN;
    v_clinic_timezone
VARCHAR(80);
    v_local_start
TIMESTAMP;
    v_local_end
TIMESTAMP;
BEGIN
SELECT s.blocks_schedule
INTO v_blocks_schedule
FROM appointment_status s
WHERE s.id = NEW.status_id;

IF
v_blocks_schedule IS NULL THEN
        RAISE EXCEPTION 'Status de agendamento invalido.';
END IF;

    NEW.blocks_schedule
= v_blocks_schedule;

SELECT c.timezone
INTO v_clinic_timezone
FROM clinic c
WHERE c.id = NEW.clinic_id;

IF
v_clinic_timezone IS NULL THEN
        RAISE EXCEPTION 'Clinica invalida.';
END IF;

    PERFORM
pg_advisory_xact_lock(hashtext(NEW.clinic_id::TEXT || ':' || NEW.professional_id::TEXT));

    IF
NEW.workplace_id IS NOT NULL THEN
        PERFORM pg_advisory_xact_lock(hashtext(NEW.clinic_id::TEXT || ':' || NEW.workplace_id::TEXT));
END IF;

    IF
NEW.blocks_schedule THEN
        v_local_start = NEW.start_datetime AT TIME ZONE v_clinic_timezone;
        v_local_end
= NEW.end_datetime AT TIME ZONE v_clinic_timezone;

        IF
v_local_start::DATE <> v_local_end::DATE THEN
            RAISE EXCEPTION 'Agendamento deve iniciar e terminar no mesmo dia local da clinica.';
END IF;

        IF
EXISTS (
            SELECT 1
              FROM schedule_block sb
             WHERE sb.clinic_id = NEW.clinic_id
               AND (sb.professional_id IS NULL OR sb.professional_id = NEW.professional_id)
               AND (NEW.workplace_id IS NULL OR sb.workplace_id IS NULL OR sb.workplace_id = NEW.workplace_id)
               AND tstzrange(sb.start_datetime, sb.end_datetime, '[)')
                   && tstzrange(NEW.start_datetime, NEW.end_datetime, '[)')
        ) THEN
            RAISE EXCEPTION 'Horario bloqueado para a clinica/profissional/local.';
END IF;

        IF
NOT EXISTS (
            SELECT 1
              FROM working_hours wh
             WHERE wh.clinic_id = NEW.clinic_id
               AND wh.day_of_week = EXTRACT(DOW FROM v_local_start)::INT
               AND v_local_start::TIME >= wh.start_time
               AND v_local_end::TIME <= wh.end_time
        ) THEN
            RAISE EXCEPTION 'Agendamento fora do horario de funcionamento da clinica.';
END IF;
END IF;

RETURN NEW;
END;
$$;

CREATE
OR REPLACE FUNCTION fn_validate_clinical_visit()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
v_appointment_professional UUID;
BEGIN
    IF
NEW.appointment_id IS NOT NULL THEN
SELECT professional_id
INTO v_appointment_professional
FROM appointment
WHERE id = NEW.appointment_id
  AND patient_id = NEW.patient_id;

IF
v_appointment_professional IS NULL THEN
            RAISE EXCEPTION 'Agendamento invalido para o paciente informado.';
END IF;

        IF
v_appointment_professional <> NEW.professional_id THEN
            RAISE EXCEPTION 'Profissional do atendimento difere do profissional do agendamento.';
END IF;
END IF;

RETURN NEW;
END;
$$;

CREATE
OR REPLACE FUNCTION fn_validate_treatment_plan()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
v_visit_patient UUID;
BEGIN
    IF
NEW.completed_visit_id IS NOT NULL THEN
SELECT patient_id
INTO v_visit_patient
FROM clinical_visit
WHERE id = NEW.completed_visit_id;

IF
v_visit_patient IS NULL THEN
            RAISE EXCEPTION 'Atendimento clinico concluido invalido.';
END IF;

        IF
NOT EXISTS (
            SELECT 1
              FROM treatment_plan tp
             WHERE tp.id = NEW.treatment_plan_id
               AND tp.patient_id = v_visit_patient
        ) THEN
            RAISE EXCEPTION 'Atendimento concluido nao pertence ao paciente do plano de tratamento.';
END IF;
END IF;

RETURN NEW;
END;
$$;

CREATE
OR REPLACE FUNCTION fn_validate_invoice()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
v_invoice_sum NUMERIC(10,2);
BEGIN
SELECT COALESCE(SUM(ii.total_amount), 0)
INTO v_invoice_sum
FROM invoice_item ii
WHERE ii.invoice_id = NEW.id;

-- A soma dos itens pode ser validada pela aplicacao antes do update final.
-- Esta funcao mantem apenas o invariant dos valores da fatura.
IF
NEW.net_amount <> NEW.gross_amount - NEW.discount_amount THEN
        RAISE EXCEPTION 'Valor liquido da cobranca inconsistente.';
END IF;

RETURN NEW;
END;
$$;

CREATE
OR REPLACE FUNCTION fn_validate_payment()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
v_invoice_patient UUID;
BEGIN
SELECT patient_id
INTO v_invoice_patient
FROM invoice
WHERE id = NEW.invoice_id;

IF
v_invoice_patient IS NULL THEN
        RAISE EXCEPTION 'Cobranca invalida.';
END IF;

    IF
v_invoice_patient <> NEW.patient_id THEN
        RAISE EXCEPTION 'Pagamento nao pertence ao paciente da cobranca.';
END IF;

RETURN NEW;
END;
$$;

CREATE
OR REPLACE FUNCTION fn_audit_row()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
v_actor UUID;
    v_row_id
UUID;
BEGIN
BEGIN
        v_actor
= NULLIF(current_setting('app.current_user_id', TRUE), '')::UUID;
EXCEPTION WHEN OTHERS THEN
        v_actor = NULL;
END;

    IF
TG_OP = 'INSERT' THEN
        v_row_id = NEW.id;
INSERT INTO audit_log(table_name, row_id, operation, actor_user_id, old_data, new_data)
VALUES (TG_TABLE_NAME, v_row_id, TG_OP, v_actor, NULL, to_jsonb(NEW));
RETURN NEW;
ELSIF
TG_OP = 'UPDATE' THEN
        v_row_id = NEW.id;
INSERT INTO audit_log(table_name, row_id, operation, actor_user_id, old_data, new_data)
VALUES (TG_TABLE_NAME, v_row_id, TG_OP, v_actor, to_jsonb(OLD), to_jsonb(NEW));
RETURN NEW;
ELSIF
TG_OP = 'DELETE' THEN
        v_row_id = OLD.id;
INSERT INTO audit_log(table_name, row_id, operation, actor_user_id, old_data, new_data)
VALUES (TG_TABLE_NAME, v_row_id, TG_OP, v_actor, to_jsonb(OLD), NULL);
RETURN OLD;
END IF;

RETURN NULL;
END;
$$;

-- ============================================================
-- UPDATED_AT TRIGGERS
-- ============================================================

CREATE TRIGGER trg_address_updated_at
    BEFORE UPDATE
    ON address
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_app_user_updated_at
    BEFORE UPDATE
    ON app_user
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

CREATE TRIGGER trg_patient_updated_at
    BEFORE UPDATE
    ON patient
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_medical_record_updated_at
    BEFORE UPDATE
    ON medical_record
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_service_updated_at
    BEFORE UPDATE
    ON service
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_equipment_updated_at
    BEFORE UPDATE
    ON equipment
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_appointment_updated_at
    BEFORE UPDATE
    ON appointment
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_clinical_visit_updated_at
    BEFORE UPDATE
    ON clinical_visit
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_treatment_plan_updated_at
    BEFORE UPDATE
    ON treatment_plan
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_insurance_provider_updated_at
    BEFORE UPDATE
    ON insurance_provider
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_patient_insurance_updated_at
    BEFORE UPDATE
    ON patient_insurance
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_invoice_updated_at
    BEFORE UPDATE
    ON invoice
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_payment_updated_at
    BEFORE UPDATE
    ON payment
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_blog_post_updated_at
    BEFORE UPDATE
    ON blog_post
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- ============================================================
-- BUSINESS TRIGGERS
-- ============================================================

CREATE TRIGGER trg_validate_appointment
    BEFORE INSERT OR
UPDATE ON appointment
    FOR EACH ROW EXECUTE FUNCTION fn_validate_appointment();

CREATE TRIGGER trg_validate_clinical_visit
    BEFORE INSERT OR
UPDATE ON clinical_visit
    FOR EACH ROW EXECUTE FUNCTION fn_validate_clinical_visit();

CREATE TRIGGER trg_validate_treatment_plan_item
    BEFORE INSERT OR
UPDATE ON treatment_plan_item
    FOR EACH ROW EXECUTE FUNCTION fn_validate_treatment_plan();

CREATE TRIGGER trg_validate_invoice
    BEFORE INSERT OR
UPDATE ON invoice
    FOR EACH ROW EXECUTE FUNCTION fn_validate_invoice();

CREATE TRIGGER trg_validate_payment
    BEFORE INSERT OR
UPDATE ON payment
    FOR EACH ROW EXECUTE FUNCTION fn_validate_payment();

-- ============================================================
-- AUDIT TRIGGERS
-- ============================================================

CREATE TRIGGER trg_audit_app_user
    AFTER INSERT OR
UPDATE OR
DELETE
ON app_user
    FOR EACH ROW EXECUTE FUNCTION fn_audit_row();

CREATE TRIGGER trg_audit_patient
    AFTER INSERT OR
UPDATE OR
DELETE
ON patient
    FOR EACH ROW EXECUTE FUNCTION fn_audit_row();

CREATE TRIGGER trg_audit_medical_record
    AFTER INSERT OR
UPDATE OR
DELETE
ON medical_record
    FOR EACH ROW EXECUTE FUNCTION fn_audit_row();

CREATE TRIGGER trg_audit_medical_record_note
    AFTER INSERT OR
UPDATE OR
DELETE
ON medical_record_note
    FOR EACH ROW EXECUTE FUNCTION fn_audit_row();

CREATE TRIGGER trg_audit_medical_record_attachment
    AFTER INSERT OR
UPDATE OR
DELETE
ON medical_record_attachment
    FOR EACH ROW EXECUTE FUNCTION fn_audit_row();

CREATE TRIGGER trg_audit_appointment
    AFTER INSERT OR
UPDATE OR
DELETE
ON appointment
    FOR EACH ROW EXECUTE FUNCTION fn_audit_row();

CREATE TRIGGER trg_audit_clinical_visit
    AFTER INSERT OR
UPDATE OR
DELETE
ON clinical_visit
    FOR EACH ROW EXECUTE FUNCTION fn_audit_row();

CREATE TRIGGER trg_audit_treatment_plan
    AFTER INSERT OR
UPDATE OR
DELETE
ON treatment_plan
    FOR EACH ROW EXECUTE FUNCTION fn_audit_row();

CREATE TRIGGER trg_audit_treatment_plan_item
    AFTER INSERT OR
UPDATE OR
DELETE
ON treatment_plan_item
    FOR EACH ROW EXECUTE FUNCTION fn_audit_row();

CREATE TRIGGER trg_audit_invoice
    AFTER INSERT OR
UPDATE OR
DELETE
ON invoice
    FOR EACH ROW EXECUTE FUNCTION fn_audit_row();

CREATE TRIGGER trg_audit_payment
    AFTER INSERT OR
UPDATE OR
DELETE
ON payment
    FOR EACH ROW EXECUTE FUNCTION fn_audit_row();

CREATE TRIGGER trg_audit_patient_consent
    AFTER INSERT OR
UPDATE OR
DELETE
ON patient_consent
    FOR EACH ROW EXECUTE FUNCTION fn_audit_row();

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_app_user_email ON app_user (email);
CREATE INDEX idx_app_user_keycloak_subject ON app_user (keycloak_subject);

CREATE INDEX idx_address_city_state ON address (city, state);

CREATE INDEX idx_patient_cpf ON patient (cpf);
CREATE INDEX idx_patient_name ON patient (full_name);
CREATE INDEX idx_patient_active ON patient (active);

CREATE INDEX idx_mr_patient ON medical_record (patient_id);
CREATE INDEX idx_mrn_record ON medical_record_note (medical_record_id);

CREATE INDEX idx_stored_file_bucket_key ON stored_file (bucket_name, object_key);
CREATE INDEX idx_mra_record ON medical_record_attachment (medical_record_id);
CREATE INDEX idx_mra_file ON medical_record_attachment (stored_file_id);

CREATE INDEX idx_professional_clinic ON professional (clinic_id);
CREATE INDEX idx_professional_specialty ON professional (specialty_id);

CREATE INDEX idx_service_category ON service (category_id);
CREATE INDEX idx_service_active ON service (active);
CREATE INDEX idx_service_price_service ON service_price (service_id);
CREATE INDEX idx_service_cost_service ON service_cost (service_id);
CREATE INDEX idx_service_cost_type ON service_cost (cost_type_id);

CREATE INDEX idx_equipment_clinic ON equipment (clinic_id);
CREATE INDEX idx_workplace_clinic ON workplace (clinic_id);

CREATE INDEX idx_working_hours_clinic_day ON working_hours (clinic_id, day_of_week);

CREATE INDEX idx_schedule_block_clinic ON schedule_block (clinic_id);
CREATE INDEX idx_schedule_block_professional ON schedule_block (professional_id);
CREATE INDEX idx_schedule_block_workplace ON schedule_block (workplace_id);
CREATE INDEX idx_schedule_block_period_gist
    ON schedule_block USING gist (tstzrange(start_datetime, end_datetime, '[)'));

CREATE INDEX idx_appointment_patient ON appointment (patient_id);
CREATE INDEX idx_appointment_clinic ON appointment (clinic_id);
CREATE INDEX idx_appointment_professional ON appointment (professional_id);
CREATE INDEX idx_appointment_workplace ON appointment (workplace_id);
CREATE INDEX idx_appointment_status ON appointment (status_id);
CREATE INDEX idx_appointment_start ON appointment (start_datetime);
CREATE INDEX idx_appointment_period_gist
    ON appointment USING gist (tstzrange(start_datetime, end_datetime, '[)'));
CREATE INDEX idx_appointment_external_calendar
    ON appointment (calendar_provider_id, external_calendar_event_id) WHERE external_calendar_event_id IS NOT NULL;

CREATE INDEX idx_appointment_service_service ON appointment_service (service_id);

CREATE INDEX idx_clinical_visit_patient ON clinical_visit (patient_id);
CREATE INDEX idx_clinical_visit_professional ON clinical_visit (professional_id);
CREATE INDEX idx_clinical_visit_record ON clinical_visit (medical_record_id);
CREATE INDEX idx_clinical_visit_status ON clinical_visit (status_id);
CREATE INDEX idx_cva_visit ON clinical_visit_attachment (clinical_visit_id);
CREATE INDEX idx_cva_file ON clinical_visit_attachment (stored_file_id);

CREATE INDEX idx_odontogram_record ON odontogram_entry (medical_record_id);
CREATE INDEX idx_odontogram_patient ON odontogram_entry (patient_id);
CREATE INDEX idx_odontogram_tooth ON odontogram_entry (tooth_number);
CREATE INDEX idx_treatment_plan_record ON treatment_plan (medical_record_id);
CREATE INDEX idx_treatment_plan_patient ON treatment_plan (patient_id);
CREATE INDEX idx_treatment_plan_status ON treatment_plan (status_id);
CREATE INDEX idx_treatment_plan_item_plan ON treatment_plan_item (treatment_plan_id);

CREATE INDEX idx_insurance_provider_active ON insurance_provider (active);
CREATE INDEX idx_patient_insurance_patient ON patient_insurance (patient_id);
CREATE INDEX idx_patient_insurance_provider ON patient_insurance (insurance_provider_id);
CREATE INDEX idx_patient_consent_patient ON patient_consent (patient_id);
CREATE INDEX idx_patient_consent_type ON patient_consent (consent_type_id);

CREATE INDEX idx_invoice_patient ON invoice (patient_id);
CREATE INDEX idx_invoice_appointment ON invoice (appointment_id);
CREATE INDEX idx_invoice_status ON invoice (status_id);
CREATE INDEX idx_invoice_due_date ON invoice (due_date);
CREATE INDEX idx_invoice_item_invoice ON invoice_item (invoice_id);

CREATE INDEX idx_payment_invoice ON payment (invoice_id);
CREATE INDEX idx_payment_patient ON payment (patient_id);
CREATE INDEX idx_payment_status ON payment (status_id);
CREATE INDEX idx_payment_paid_at ON payment (paid_at);

CREATE INDEX idx_blog_post_status ON blog_post (status_id);
CREATE INDEX idx_blog_post_slug ON blog_post (slug);
CREATE INDEX idx_social_link_clinic ON social_link (clinic_id);

CREATE INDEX idx_audit_table_row ON audit_log (table_name, row_id);
CREATE INDEX idx_audit_created_at ON audit_log (created_at);
CREATE INDEX idx_audit_actor ON audit_log (actor_user_id);

-- ============================================================
-- SEED DATA
-- ============================================================

INSERT INTO role (code, name)
VALUES ('ADMIN', 'Administrador'),
       ('PROFESSIONAL', 'Profissional'),
       ('RECEPTIONIST', 'Recepcionista'),
       ('FINANCIAL', 'Financeiro') ON CONFLICT (code) DO NOTHING;

INSERT INTO appointment_status (code, name, blocks_schedule, final_status)
VALUES ('SCHEDULED', 'Agendado', TRUE, FALSE),
       ('CONFIRMED', 'Confirmado', TRUE, FALSE),
       ('IN_PROGRESS', 'Em atendimento', TRUE, FALSE),
       ('COMPLETED', 'Concluido', FALSE, TRUE),
       ('CANCELLED', 'Cancelado', FALSE, TRUE),
       ('NO_SHOW', 'Nao compareceu', FALSE, TRUE) ON CONFLICT (code) DO NOTHING;

INSERT INTO clinical_visit_status (code, name)
VALUES ('OPEN', 'Aberto'),
       ('IN_PROGRESS', 'Em andamento'),
       ('FINISHED', 'Finalizado'),
       ('CANCELLED', 'Cancelado') ON CONFLICT (code) DO NOTHING;

INSERT INTO treatment_plan_status (code, name)
VALUES ('DRAFT', 'Rascunho'),
       ('PRESENTED', 'Apresentado'),
       ('APPROVED', 'Aprovado'),
       ('IN_PROGRESS', 'Em andamento'),
       ('COMPLETED', 'Concluido'),
       ('CANCELLED', 'Cancelado') ON CONFLICT (code) DO NOTHING;

INSERT INTO tooth_condition (code, name)
VALUES ('HEALTHY', 'Saudavel'),
       ('CARIES', 'Carie'),
       ('RESTORED', 'Restaurado'),
       ('MISSING', 'Ausente'),
       ('IMPLANT', 'Implante'),
       ('EXTRACTION_INDICATED', 'Extracao indicada'),
       ('ROOT_CANAL', 'Canal tratado'),
       ('OBSERVATION', 'Observacao') ON CONFLICT (code) DO NOTHING;

INSERT INTO tooth_surface (code, name)
VALUES ('M', 'Mesial'),
       ('D', 'Distal'),
       ('V', 'Vestibular'),
       ('L', 'Lingual'),
       ('P', 'Palatina'),
       ('O', 'Oclusal'),
       ('I', 'Incisal') ON CONFLICT (code) DO NOTHING;

INSERT INTO calendar_provider (code, name)
VALUES ('GOOGLE', 'Google Agenda'),
       ('OUTLOOK', 'Microsoft Outlook'),
       ('MANUAL', 'Manual') ON CONFLICT (code) DO NOTHING;

INSERT INTO calendar_sync_status (code, name)
VALUES ('PENDING', 'Pendente'),
       ('SYNCED', 'Sincronizado'),
       ('FAILED', 'Falhou'),
       ('NOT_SYNCED', 'Nao sincronizado') ON CONFLICT (code) DO NOTHING;

INSERT INTO invoice_status (code, name)
VALUES ('OPEN', 'Aberta'),
       ('PARTIALLY_PAID', 'Parcialmente paga'),
       ('PAID', 'Paga'),
       ('OVERDUE', 'Vencida'),
       ('CANCELLED', 'Cancelada') ON CONFLICT (code) DO NOTHING;

INSERT INTO payment_status (code, name)
VALUES ('PENDING', 'Pendente'),
       ('CONFIRMED', 'Confirmado'),
       ('FAILED', 'Falhou'),
       ('CANCELLED', 'Cancelado'),
       ('REFUNDED', 'Reembolsado') ON CONFLICT (code) DO NOTHING;

INSERT INTO payment_method (code, name)
VALUES ('CASH', 'Dinheiro'),
       ('PIX', 'PIX'),
       ('CREDIT_CARD', 'Cartao de credito'),
       ('DEBIT_CARD', 'Cartao de debito'),
       ('BANK_TRANSFER', 'Transferencia bancaria'),
       ('INSURANCE', 'Convenio') ON CONFLICT (code) DO NOTHING;

INSERT INTO consent_type (code, name, description)
VALUES ('DATA_PROCESSING', 'Tratamento de dados pessoais',
        'Consentimento para tratamento de dados pessoais conforme LGPD.'),
       ('IMAGE_STORAGE', 'Armazenamento de imagens e exames',
        'Consentimento para armazenamento de imagens, exames e anexos clinicos.'),
       ('MARKETING', 'Comunicacoes de marketing', 'Consentimento para envio de comunicacoes promocionais.'),
       ('TREATMENT', 'Tratamento odontologico',
        'Consentimento informado para realizacao de tratamento odontologico.') ON CONFLICT (code) DO NOTHING;

INSERT INTO blog_post_status (code, name)
VALUES ('DRAFT', 'Rascunho'),
       ('PUBLISHED', 'Publicado'),
       ('ARCHIVED', 'Arquivado') ON CONFLICT (code) DO NOTHING;

INSERT INTO service_category (code, name)
VALUES ('PREVENTIVE', 'Preventivo'),
       ('RESTORATIVE', 'Restaurador'),
       ('AESTHETIC', 'Estetico'),
       ('ORTHODONTICS', 'Ortodontia'),
       ('SURGERY', 'Cirurgia'),
       ('ENDODONTICS', 'Endodontia'),
       ('PERIODONTICS', 'Periodontia'),
       ('IMPLANT', 'Implantodontia') ON CONFLICT (code) DO NOTHING;

INSERT INTO service_cost_type (code, name)
VALUES ('MATERIAL', 'Material'),
       ('LABORATORY', 'Laboratorio'),
       ('PROFESSIONAL', 'Profissional'),
       ('EQUIPMENT', 'Equipamento'),
       ('OPERATIONAL', 'Operacional') ON CONFLICT (code) DO NOTHING;

INSERT INTO social_platform (code, name)
VALUES ('INSTAGRAM', 'Instagram'),
       ('FACEBOOK', 'Facebook'),
       ('WHATSAPP', 'WhatsApp'),
       ('TIKTOK', 'TikTok'),
       ('YOUTUBE', 'YouTube'),
       ('LINKEDIN', 'LinkedIn') ON CONFLICT (code) DO NOTHING;

INSERT INTO specialty (code, name)
VALUES ('GENERAL_DENTISTRY', 'Clinico geral'),
       ('ORTHODONTICS', 'Ortodontia'),
       ('ENDODONTICS', 'Endodontia'),
       ('PERIODONTICS', 'Periodontia'),
       ('IMPLANTOLOGY', 'Implantodontia'),
       ('ORAL_SURGERY', 'Cirurgia oral'),
       ('PEDIATRIC_DENTISTRY', 'Odontopediatria'),
       ('PROSTHODONTICS', 'Protese dentaria'),
       ('AESTHETIC_DENTISTRY', 'Odontologia estetica') ON CONFLICT (code) DO NOTHING;
