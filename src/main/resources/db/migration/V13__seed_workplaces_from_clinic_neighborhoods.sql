INSERT INTO workplace (clinic_id, name, description, active)
SELECT id,
       COALESCE(NULLIF(TRIM(neighborhood), ''), name) AS name,
       'Local de atendimento - ' || COALESCE(NULLIF(TRIM(neighborhood), ''), name) AS description,
       TRUE
FROM clinic
WHERE active = TRUE
ON CONFLICT (clinic_id, name) DO NOTHING;
