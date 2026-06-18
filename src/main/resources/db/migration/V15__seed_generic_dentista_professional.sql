-- Cria o usuário genérico "Dentista" (caso não exista)
INSERT INTO app_user (id, keycloak_subject, keycloak_username, full_name, email, email_verified, active)
VALUES ('00000000-0000-0000-0000-000000000001',
        'generic-dentista',
        'dentista',
        'Dentista',
        'dentista@clinica.local',
        TRUE,
        TRUE)
ON CONFLICT (keycloak_subject) DO NOTHING;

-- Cria o profissional vinculado à primeira clínica ativa disponível e à especialidade Odontologia
INSERT INTO professional (user_id, clinic_id, specialty_id, license_number, bio, active)
SELECT '00000000-0000-0000-0000-000000000001',
       c.id,
       s.id,
       'CRO000001',
       'Profissional genérico criado automaticamente pelo sistema.',
       TRUE
FROM clinic c
         CROSS JOIN specialty s
WHERE c.active = TRUE
  AND s.code = 'DENTISTRY'
  AND NOT EXISTS (SELECT 1
                  FROM professional
                  WHERE user_id = '00000000-0000-0000-0000-000000000001')
ORDER BY c.created_at
LIMIT 1;

-- Registra o vínculo na tabela de memberships (professional_clinic) como clínica primária
INSERT INTO professional_clinic (professional_id, clinic_id, primary_clinic, active)
SELECT p.id, p.clinic_id, TRUE, TRUE
FROM professional p
WHERE p.user_id = '00000000-0000-0000-0000-000000000001'
ON CONFLICT (professional_id, clinic_id) DO NOTHING;
