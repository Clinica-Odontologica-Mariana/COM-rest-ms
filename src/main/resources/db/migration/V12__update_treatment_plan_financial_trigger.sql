DROP TRIGGER IF EXISTS trg_after_insert_treatment_plan_item ON public.treatment_plan_item;
DROP FUNCTION IF EXISTS fn_create_financial_from_treatment_item();

CREATE OR REPLACE FUNCTION fn_create_financial_from_treatment_item()
RETURNS TRIGGER AS $$
DECLARE
v_clinic_id          UUID;
    v_patient_id         UUID;
    v_professional_id    UUID;
    v_plan_title         VARCHAR(150);
    v_created_by_user_id UUID;
    v_patient_name       VARCHAR(150);
    v_description        VARCHAR(255);
    v_old_description    VARCHAR(255);
BEGIN
SELECT tp.patient_id, tp.professional_id, tp.title, tp.created_by_user_id
INTO v_patient_id, v_professional_id, v_plan_title, v_created_by_user_id
FROM public.treatment_plan tp
WHERE tp.id = NEW.treatment_plan_id;

IF v_professional_id IS NOT NULL THEN
SELECT clinic_id INTO v_clinic_id FROM public.professional WHERE id = v_professional_id;
END IF;

IF v_clinic_id IS NULL THEN
    SELECT id INTO v_clinic_id FROM public.clinic LIMIT 1;
END IF;

SELECT full_name INTO v_patient_name FROM public.patient WHERE id = v_patient_id;

v_description := LEFT(
        'Procedimento: ' || COALESCE(NEW.description, 'Sem descrição')
        || CASE WHEN NEW.tooth_number IS NOT NULL THEN ' (Dente ' || NEW.tooth_number || ')' ELSE '' END
        || ' | Plano: ' || COALESCE(v_plan_title, '-')
        || ' | Paciente: ' || COALESCE(v_patient_name, 'Desconhecido'),
        255
    );

    IF TG_OP = 'INSERT' THEN
        IF NEW.estimated_price IS NOT NULL AND NEW.estimated_price > 0 AND v_clinic_id IS NOT NULL THEN
            INSERT INTO public.financial_transaction (
                id, clinic_id, appointment_id, treatment_plan_id, description,
                type, category, amount, status, transaction_date,
                created_by_user_id, created_at, updated_at
            ) VALUES (
                gen_random_uuid(), v_clinic_id, NULL, NEW.treatment_plan_id, v_description,
                'RECEITA', 'Tratamentos', NEW.estimated_price, 'PENDING', CURRENT_DATE,
                v_created_by_user_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            );
END IF;

    ELSIF TG_OP = 'UPDATE' THEN

        v_old_description := LEFT(
            'Procedimento: ' || COALESCE(OLD.description, 'Sem descrição')
            || CASE WHEN OLD.tooth_number IS NOT NULL THEN ' (Dente ' || OLD.tooth_number || ')' ELSE '' END
            || ' | Plano: ' || COALESCE(v_plan_title, '-')
            || ' | Paciente: ' || COALESCE(v_patient_name, 'Desconhecido'),
            255
        );

        IF NEW.estimated_price IS NOT NULL AND NEW.estimated_price > 0 AND v_clinic_id IS NOT NULL THEN
UPDATE public.financial_transaction
SET amount = NEW.estimated_price,
    description = v_description,
    updated_at = CURRENT_TIMESTAMP
WHERE treatment_plan_id = NEW.treatment_plan_id
  AND description LIKE 'Procedimento: ' || COALESCE(OLD.description, 'Sem descrição') || '%';
END IF;

END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_after_insert_treatment_plan_item
    AFTER INSERT OR UPDATE ON public.treatment_plan_item
                        FOR EACH ROW
                        EXECUTE FUNCTION fn_create_financial_from_treatment_item();