INSERT INTO clinical_procedure (id, name, category, active)
VALUES ('11111111-0000-0000-0000-000000000001', 'Extração de Siso',       'Cirurgia',    TRUE),
       ('11111111-0000-0000-0000-000000000002', 'Clareamento Dental',     'Estética',    TRUE),
       ('11111111-0000-0000-0000-000000000003', 'Restauração em Resina',  'Restauração', TRUE),
       ('11111111-0000-0000-0000-000000000004', 'Implante Dentário',      'Implante',    TRUE),
       ('11111111-0000-0000-0000-000000000005', 'Coroa de Porcelana',     'Prótese',     TRUE),
       ('11111111-0000-0000-0000-000000000006', 'Limpeza e Profilaxia',   'Prevenção',   TRUE),
       ('11111111-0000-0000-0000-000000000007', 'Tratamento de Canal',    'Endodontia',  TRUE),
       ('11111111-0000-0000-0000-000000000008', 'Faceta de Porcelana',    'Estética',    TRUE),
       ('11111111-0000-0000-0000-000000000009', 'Aparelho Ortodôntico',   'Ortodontia',  TRUE),
       ('11111111-0000-0000-0000-000000000010', 'Contenção Fixa',         'Ortodontia',  TRUE),
       ('11111111-0000-0000-0000-000000000011', 'Raspagem Periodontal',   'Periodontia', TRUE),
       ('11111111-0000-0000-0000-000000000012', 'Manutenção Periodontal', 'Periodontia', TRUE)
ON CONFLICT (id) DO NOTHING;
