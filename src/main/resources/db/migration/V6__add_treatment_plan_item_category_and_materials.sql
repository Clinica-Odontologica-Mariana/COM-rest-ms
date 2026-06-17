ALTER TABLE treatment_plan_item
    ADD COLUMN category VARCHAR(100);

CREATE TABLE treatment_plan_item_material
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    item_id    UUID        NOT NULL,
    name       VARCHAR(255) NOT NULL,
    category   VARCHAR(100),
    quantity   INT         NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_treatment_plan_item_material_quantity CHECK (quantity >= 1),
    CONSTRAINT fk_treatment_plan_item_material_item FOREIGN KEY (item_id) REFERENCES treatment_plan_item (id) ON DELETE CASCADE
);
