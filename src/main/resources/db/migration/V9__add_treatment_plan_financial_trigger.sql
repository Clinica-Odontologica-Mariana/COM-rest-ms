CREATE OR REPLACE FUNCTION fn_create_financial_from_treatment()
RETURNS TRIGGER AS $$
DECLARE
v_clinic_id UUID;
    v_patient_name VARCHAR(150);
BEGIN
    IF NEW.professional_id IS NOT NULL THEN
SELECT clinic_id INTO v_clinic_id FROM public.professional WHERE id = NEW.professional_id;
END IF;

    IF v_clinic_id IS NULL THEN
SELECT clinic_id INTO v_clinic_id FROM public.patient_clinic
WHERE patient_id = NEW.patient_id AND primary_clinic = TRUE LIMIT 1;
END IF;

SELECT full_name INTO v_patient_name FROM public.patient WHERE id = NEW.patient_id;

IF NEW.total_amount > 0 AND v_clinic_id IS NOT NULL THEN
        INSERT INTO public.financial_transaction (
            id, clinic_id, appointment_id, treatment_plan_id, description,
            type, category, amount, status, transaction_date,
            notes, created_by_user_id, created_at, updated_at
        ) VALUES (
            gen_random_uuid(),
            v_clinic_id,
            NULL,
            NEW.id,
            'Plano: ' || NEW.title || ' | Paciente: ' || COALESCE(v_patient_name, 'Desconhecido'),
            'RECEITA',
            'Tratamentos',
            NEW.total_amount,
            'PAID',
            CURRENT_DATE,
            'Gerado automaticamente via trigger.',
            NEW.created_by_user_id,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_after_insert_treatment_plan ON public.treatment_plan;

CREATE TRIGGER trg_after_insert_treatment_plan
    AFTER INSERT ON public.treatment_plan
    FOR EACH ROW
    EXECUTE FUNCTION fn_create_financial_from_treatment();