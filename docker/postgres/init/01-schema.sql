CREATE
EXTENSION IF NOT EXISTS "pgcrypto";
CREATE
EXTENSION IF NOT EXISTS "btree_gist";

-- =========================
-- DOMAIN TABLES (3FN)
-- =========================

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
    blocks_schedule BOOLEAN     NOT NULL DEFAULT TRUE
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

-- =========================
-- SECURITY / USERS
-- =========================

CREATE TABLE app_user
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    full_name      VARCHAR(150) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
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

-- =========================
-- CORE ENTITIES
-- =========================

CREATE TABLE clinic
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name           VARCHAR(150) NOT NULL,
    document       VARCHAR(20),
    phone          VARCHAR(20),
    email          VARCHAR(150),
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_clinic_document UNIQUE (document),
    CONSTRAINT chk_clinic_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
            )
);

CREATE TABLE workplace
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    clinic_id      UUID         NOT NULL,
    name           VARCHAR(100) NOT NULL,
    description    TEXT,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_workplace_name_per_clinic UNIQUE (clinic_id, name),
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
    id             UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    user_id        UUID      NOT NULL UNIQUE,
    clinic_id      UUID      NOT NULL,
    license_number VARCHAR(50),
    specialty      VARCHAR(100),
    active         BOOLEAN   NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMP,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_professional_license UNIQUE (clinic_id, license_number),
    CONSTRAINT chk_professional_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
            ),
    CONSTRAINT fk_professional_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE RESTRICT,
    CONSTRAINT fk_professional_clinic FOREIGN KEY (clinic_id) REFERENCES clinic (id) ON DELETE RESTRICT
);

CREATE TABLE patient
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    full_name      VARCHAR(150) NOT NULL,
    cpf            VARCHAR(11)  NOT NULL UNIQUE,
    phone          VARCHAR(20)  NOT NULL,
    email          VARCHAR(150),
    birth_date     DATE,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_patient_cpf_format CHECK (cpf ~ '^[0-9]{11}$'
) ,
    CONSTRAINT chk_patient_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
        )
);

CREATE TABLE medical_record
(
    id         UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    patient_id UUID      NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medical_record_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id)
            ON DELETE RESTRICT
);

CREATE TABLE medical_record_note
(
    id                UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    medical_record_id UUID      NOT NULL,
    note              TEXT      NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mrn_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_record (id)
            ON DELETE CASCADE
);

CREATE TABLE medical_record_image
(
    id                UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    medical_record_id UUID      NOT NULL,
    file_url          TEXT      NOT NULL,
    description       TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_image_medical_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_record (id)
            ON DELETE CASCADE
);

CREATE TABLE service
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    category_id    UUID         NOT NULL,
    name           VARCHAR(150) NOT NULL,
    description    TEXT,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_service_name_per_category UNIQUE (category_id, name),
    CONSTRAINT chk_service_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
            ),
    CONSTRAINT fk_service_category FOREIGN KEY (category_id) REFERENCES service_category (id)
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
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_service_cost_amount CHECK (amount >= 0),
    CONSTRAINT chk_service_cost_period CHECK (valid_to IS NULL OR valid_to >= valid_from),
    CONSTRAINT fk_cost_service
        FOREIGN KEY (service_id) REFERENCES service (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_cost_type FOREIGN KEY (cost_type_id) REFERENCES service_cost_type (id)
);

CREATE TABLE equipment
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    clinic_id      UUID         NOT NULL,
    name           VARCHAR(150) NOT NULL,
    description    TEXT,
    location       VARCHAR(100),
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
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

CREATE TABLE schedule_block
(
    id              UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    clinic_id       UUID      NOT NULL,
    workplace_id    UUID,
    professional_id UUID,
    start_datetime  TIMESTAMP NOT NULL,
    end_datetime    TIMESTAMP NOT NULL,
    reason          VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_schedule_block_period CHECK (end_datetime > start_datetime),
    CONSTRAINT fk_block_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_block_workplace
        FOREIGN KEY (workplace_id) REFERENCES workplace (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_block_professional
        FOREIGN KEY (professional_id) REFERENCES professional (id)
            ON DELETE SET NULL
);

CREATE TABLE appointment
(
    id              UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    patient_id      UUID      NOT NULL,
    clinic_id       UUID      NOT NULL,
    workplace_id    UUID,
    professional_id UUID      NOT NULL,
    status_id       UUID      NOT NULL,
    start_datetime  TIMESTAMP NOT NULL,
    end_datetime    TIMESTAMP NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_appointment_period CHECK (end_datetime > start_datetime),
    CONSTRAINT fk_appointment_patient
        FOREIGN KEY (patient_id) REFERENCES patient (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_workplace
        FOREIGN KEY (workplace_id) REFERENCES workplace (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_appointment_professional
        FOREIGN KEY (professional_id) REFERENCES professional (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_status
        FOREIGN KEY (status_id) REFERENCES appointment_status (id)
            ON DELETE RESTRICT
);

CREATE TABLE blog_post
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    status_id      UUID         NOT NULL,
    title          VARCHAR(200) NOT NULL,
    content        TEXT         NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    inactivated_at TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_blog_post_inactivation
        CHECK (
            (active = TRUE AND inactivated_at IS NULL) OR
            (active = FALSE AND inactivated_at IS NOT NULL)
            ),
    CONSTRAINT fk_blog_status FOREIGN KEY (status_id) REFERENCES blog_post_status (id)
);

CREATE TABLE social_link
(
    id          UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    clinic_id   UUID      NOT NULL,
    platform_id UUID      NOT NULL,
    url         TEXT      NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_social_link UNIQUE (clinic_id, platform_id),
    CONSTRAINT fk_social_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_social_platform FOREIGN KEY (platform_id) REFERENCES social_platform (id)
);

-- =========================
-- AUDIT TRIGGERS (TIMESTAMPS ONLY)
-- =========================

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

CREATE TRIGGER trg_blog_post_updated_at
    BEFORE UPDATE
    ON blog_post
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- =========================
-- APPOINTMENT BUSINESS RULES
-- =========================

CREATE
OR REPLACE FUNCTION fn_validate_appointment()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
v_new_blocks BOOLEAN;
BEGIN
SELECT blocks_schedule
INTO v_new_blocks
FROM appointment_status
WHERE id = NEW.status_id;

IF
COALESCE(v_new_blocks, FALSE) THEN
        IF EXISTS (
            SELECT 1
              FROM appointment a
              JOIN appointment_status s ON s.id = a.status_id
             WHERE a.id <> COALESCE(NEW.id, '00000000-0000-0000-0000-000000000000'::UUID)
               AND a.clinic_id = NEW.clinic_id
               AND a.professional_id = NEW.professional_id
               AND s.blocks_schedule = TRUE
               AND tsrange(a.start_datetime, a.end_datetime, '[)') && tsrange(NEW.start_datetime, NEW.end_datetime, '[)')
        ) THEN
            RAISE EXCEPTION 'Conflito de horario para o profissional na clinica.';
END IF;

        IF
EXISTS (
            SELECT 1
              FROM schedule_block sb
             WHERE sb.clinic_id = NEW.clinic_id
               AND (sb.professional_id IS NULL OR sb.professional_id = NEW.professional_id)
               AND (NEW.workplace_id IS NULL OR sb.workplace_id IS NULL OR sb.workplace_id = NEW.workplace_id)
               AND tsrange(sb.start_datetime, sb.end_datetime, '[)') && tsrange(NEW.start_datetime, NEW.end_datetime, '[)')
        ) THEN
            RAISE EXCEPTION 'Horario bloqueado para a clinica/profissional/local.';
END IF;

        IF
NOT EXISTS (
            SELECT 1
              FROM working_hours wh
             WHERE wh.clinic_id = NEW.clinic_id
               AND wh.day_of_week = EXTRACT(DOW FROM NEW.start_datetime)::INT
               AND NEW.start_datetime::TIME >= wh.start_time
               AND NEW.end_datetime::TIME <= wh.end_time
        ) THEN
            RAISE EXCEPTION 'Agendamento fora do horario de funcionamento da clinica.';
END IF;
END IF;

RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validate_appointment
    BEFORE INSERT OR
UPDATE ON appointment
    FOR EACH ROW EXECUTE FUNCTION fn_validate_appointment();

-- =========================
-- INDEXES
-- =========================

CREATE INDEX idx_patient_cpf ON patient (cpf);

CREATE INDEX idx_mr_patient ON medical_record (patient_id);
CREATE INDEX idx_mrn_record ON medical_record_note (medical_record_id);
CREATE INDEX idx_mri_record ON medical_record_image (medical_record_id);

CREATE INDEX idx_service_category ON service (category_id);
CREATE INDEX idx_service_cost_service ON service_cost (service_id);
CREATE INDEX idx_service_cost_type ON service_cost (cost_type_id);

CREATE INDEX idx_equipment_clinic ON equipment (clinic_id);
CREATE INDEX idx_workplace_clinic ON workplace (clinic_id);

CREATE INDEX idx_working_hours_clinic_day ON working_hours (clinic_id, day_of_week);

CREATE INDEX idx_schedule_block_clinic ON schedule_block (clinic_id);
CREATE INDEX idx_schedule_block_period ON schedule_block (start_datetime, end_datetime);

CREATE INDEX idx_appointment_patient ON appointment (patient_id);
CREATE INDEX idx_appointment_clinic ON appointment (clinic_id);
CREATE INDEX idx_appointment_professional ON appointment (professional_id);
CREATE INDEX idx_appointment_status ON appointment (status_id);
CREATE INDEX idx_appointment_period ON appointment (start_datetime, end_datetime);

CREATE INDEX idx_social_link_clinic ON social_link (clinic_id);
