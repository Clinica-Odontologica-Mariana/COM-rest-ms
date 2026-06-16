CREATE TABLE IF NOT EXISTS working_hours
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinic_id   UUID    NOT NULL,
    day_of_week INT     NOT NULL,
    start_time  TIME    NOT NULL,
    end_time    TIME    NOT NULL,
    CONSTRAINT fk_working_hours_clinic
    FOREIGN KEY (clinic_id) REFERENCES clinic (id)
    ON DELETE CASCADE
    );