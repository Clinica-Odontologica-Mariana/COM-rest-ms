CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_role (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id) REFERENCES app_user(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES role(id)
        ON DELETE CASCADE
);

CREATE TABLE clinic (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    document VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(150),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_clinic_created_by FOREIGN KEY (created_by) REFERENCES app_user(id),
    CONSTRAINT fk_clinic_updated_by FOREIGN KEY (updated_by) REFERENCES app_user(id)
);

CREATE TABLE workplace (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinic_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_workplace_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_workplace_created_by FOREIGN KEY (created_by) REFERENCES app_user(id),
    CONSTRAINT fk_workplace_updated_by FOREIGN KEY (updated_by) REFERENCES app_user(id)
);

CREATE TABLE patient (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    birth_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_patient_created_by FOREIGN KEY (created_by) REFERENCES app_user(id),
    CONSTRAINT fk_patient_updated_by FOREIGN KEY (updated_by) REFERENCES app_user(id)
);

CREATE TABLE medical_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_medical_record_patient
        FOREIGN KEY (patient_id) REFERENCES patient(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_mr_created_by FOREIGN KEY (created_by) REFERENCES app_user(id),
    CONSTRAINT fk_mr_updated_by FOREIGN KEY (updated_by) REFERENCES app_user(id)
);

CREATE TABLE medical_record_image (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medical_record_id UUID NOT NULL,
    file_url TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT fk_image_medical_record
        FOREIGN KEY (medical_record_id) REFERENCES medical_record(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_image_created_by FOREIGN KEY (created_by) REFERENCES app_user(id)
);

CREATE TABLE service (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_service_created_by FOREIGN KEY (created_by) REFERENCES app_user(id),
    CONSTRAINT fk_service_updated_by FOREIGN KEY (updated_by) REFERENCES app_user(id)
);

CREATE TABLE service_cost (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL,
    cost_type VARCHAR(50) NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT fk_cost_service
        FOREIGN KEY (service_id) REFERENCES service(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_cost_created_by FOREIGN KEY (created_by) REFERENCES app_user(id)
);

CREATE TABLE equipment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinic_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    location VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_equipment_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_eq_created_by FOREIGN KEY (created_by) REFERENCES app_user(id),
    CONSTRAINT fk_eq_updated_by FOREIGN KEY (updated_by) REFERENCES app_user(id)
);

CREATE TABLE working_hours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinic_id UUID NOT NULL,
    day_of_week INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT fk_working_hours_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic(id)
        ON DELETE CASCADE
);

CREATE TABLE schedule_block (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinic_id UUID NOT NULL,
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT fk_block_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_block_created_by FOREIGN KEY (created_by) REFERENCES app_user(id)
);

CREATE TABLE appointment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    clinic_id UUID NOT NULL,
    workplace_id UUID,
    appointment_datetime TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_appointment_patient
        FOREIGN KEY (patient_id) REFERENCES patient(id),
    CONSTRAINT fk_appointment_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic(id),
    CONSTRAINT fk_appointment_workplace
        FOREIGN KEY (workplace_id) REFERENCES workplace(id),
    CONSTRAINT fk_app_created_by FOREIGN KEY (created_by) REFERENCES app_user(id),
    CONSTRAINT fk_app_updated_by FOREIGN KEY (updated_by) REFERENCES app_user(id)
);

CREATE TABLE blog_post (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_post_created_by FOREIGN KEY (created_by) REFERENCES app_user(id),
    CONSTRAINT fk_post_updated_by FOREIGN KEY (updated_by) REFERENCES app_user(id)
);

CREATE TABLE social_link (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinic_id UUID NOT NULL,
    platform VARCHAR(50) NOT NULL,
    url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT fk_social_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_social_created_by FOREIGN KEY (created_by) REFERENCES app_user(id)
);

CREATE INDEX idx_patient_cpf ON patient(cpf);
CREATE INDEX idx_appointment_datetime ON appointment(appointment_datetime);
CREATE INDEX idx_appointment_patient ON appointment(patient_id);
CREATE INDEX idx_appointment_clinic ON appointment(clinic_id);
CREATE INDEX idx_medical_record_patient ON medical_record(patient_id);
CREATE INDEX idx_service_cost_service ON service_cost(service_id);
CREATE INDEX idx_workplace_clinic ON workplace(clinic_id);
CREATE INDEX idx_schedule_block_clinic ON schedule_block(clinic_id);
CREATE INDEX idx_social_link_clinic ON social_link(clinic_id);
