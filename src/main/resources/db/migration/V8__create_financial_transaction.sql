CREATE TABLE financial_transaction
(
    id               UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    clinic_id        UUID           NOT NULL,
    appointment_id   UUID,
    treatment_plan_id UUID,
    description      VARCHAR(255)   NOT NULL,
    type             VARCHAR(10)    NOT NULL,
    category         VARCHAR(80),
    amount           NUMERIC(10, 2) NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    transaction_date DATE           NOT NULL,
    notes            TEXT,
    created_by_user_id UUID,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_financial_transaction_type   CHECK (type   IN ('RECEITA', 'DESPESA')),
    CONSTRAINT chk_financial_transaction_status CHECK (status IN ('PENDING', 'PAID', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_financial_transaction_amount CHECK (amount > 0),

    CONSTRAINT fk_financial_transaction_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinic (id) ON DELETE RESTRICT,
    CONSTRAINT fk_financial_transaction_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointment (id) ON DELETE SET NULL,
    CONSTRAINT fk_financial_transaction_treatment_plan
        FOREIGN KEY (treatment_plan_id) REFERENCES treatment_plan (id) ON DELETE SET NULL,
    CONSTRAINT fk_financial_transaction_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE INDEX idx_financial_transaction_clinic       ON financial_transaction (clinic_id);
CREATE INDEX idx_financial_transaction_date         ON financial_transaction (transaction_date);
CREATE INDEX idx_financial_transaction_type         ON financial_transaction (type);
CREATE INDEX idx_financial_transaction_status       ON financial_transaction (status);
CREATE INDEX idx_financial_transaction_clinic_date  ON financial_transaction (clinic_id, transaction_date);

CREATE TRIGGER trg_financial_transaction_updated_at
    BEFORE UPDATE ON financial_transaction
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();