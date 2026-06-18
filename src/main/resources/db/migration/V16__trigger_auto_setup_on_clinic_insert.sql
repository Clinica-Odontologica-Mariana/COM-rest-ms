CREATE OR REPLACE FUNCTION fn_after_clinic_insert()
    RETURNS TRIGGER AS
$$
DECLARE
    v_user_id         UUID := '00000000-0000-0000-0000-000000000001';
    v_specialty_id    UUID;
    v_professional_id UUID;
    v_workplace_name  VARCHAR(100);
BEGIN
    -- 1. Cria o workplace usando o bairro (ou nome da clínica como fallback)
    v_workplace_name := COALESCE(NULLIF(TRIM(NEW.neighborhood), ''), NEW.name);

    INSERT INTO workplace (clinic_id, name, description, active)
    VALUES (NEW.id,
            v_workplace_name,
            'Local de atendimento - ' || v_workplace_name,
            TRUE)
    ON CONFLICT (clinic_id, name) DO NOTHING;

    -- 2. Garante que o app_user genérico "Dentista" existe
    INSERT INTO app_user (id, keycloak_subject, keycloak_username, full_name, email, email_verified, active)
    VALUES (v_user_id,
            'generic-dentista',
            'dentista',
            'Dentista',
            'dentista@clinica.local',
            TRUE,
            TRUE)
    ON CONFLICT (keycloak_subject) DO NOTHING;

    -- 3. Busca a especialidade Odontologia
    SELECT id INTO v_specialty_id FROM specialty WHERE code = 'DENTISTRY' LIMIT 1;

    -- 4. Verifica se o profissional genérico já existe
    SELECT id INTO v_professional_id FROM professional WHERE user_id = v_user_id LIMIT 1;

    IF v_professional_id IS NULL THEN
        -- Primeira clínica: cria o profissional e define como clínica primária
        INSERT INTO professional (user_id, clinic_id, specialty_id, license_number, bio, active)
        VALUES (v_user_id,
                NEW.id,
                v_specialty_id,
                'CRO000001',
                'Profissional genérico criado automaticamente pelo sistema.',
                TRUE)
        RETURNING id INTO v_professional_id;

        INSERT INTO professional_clinic (professional_id, clinic_id, primary_clinic, active)
        VALUES (v_professional_id, NEW.id, TRUE, TRUE)
        ON CONFLICT (professional_id, clinic_id) DO NOTHING;
    ELSE
        -- Clínica adicional: apenas vincula sem sobrescrever a primária
        INSERT INTO professional_clinic (professional_id, clinic_id, primary_clinic, active)
        VALUES (v_professional_id, NEW.id, FALSE, TRUE)
        ON CONFLICT (professional_id, clinic_id) DO NOTHING;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_after_clinic_insert
    AFTER INSERT
    ON clinic
    FOR EACH ROW
EXECUTE FUNCTION fn_after_clinic_insert();
