-- ============================================================
--  DUMMY DATA - Plataforma de Servicios de Modelado 3D
-- ============================================================

-- ------------------------------------------------------------
--  ACCOUNT
-- ------------------------------------------------------------
INSERT INTO account (name, last_name, email, phone, password, role, specialty, portfolio_url, bio, years_experience) VALUES
('Ricardo',   'Solano Peña',       'ricardo.solano@render3d.mx',     '5512345678',  '$2a$10$dummyhash1', 'ADMIN',  NULL, NULL, NULL, NULL),
('Emilio',    'Vargas Ríos',       'emilio.vargas@email.com',        '5523456789',  '$2a$10$dummyhash2', 'EXPERT', 'Modelado orgánico y personajes',  'https://portfolio.emiliovargas.art',   'Especialista en personajes para videojuegos y cinemáticas. Trabajo con ZBrush y Blender.',         6),
('Daniela',   'Cruz Montoya',      'daniela.cruz@email.com',         '5534567890',  '$2a$10$dummyhash3', 'EXPERT', 'Texturizado y lookdev',             'https://danielacruz.artstation.com',   'Artista de superficies con dominio de Substance Painter y Mari. Enfoque en realismo fotográfico.',  4),
('Óscar',     'Bernal Lara',       'oscar.bernal@email.com',         '5545678901',  '$2a$10$dummyhash4', 'EXPERT', 'Animación 3D y rigging',            'https://oscarbernal.myportfolio.com',  'Animador con experiencia en producción de cortometrajes y publicidad. Uso de Maya y Blender.',      8),
('Mariana',   'Stein Vidal',       'mariana.stein@estudio.com',      '5556789012',  '$2a$10$dummyhash5', 'CLIENT', NULL, NULL, NULL, NULL),
('Rodrigo',   'Fuentes Alcaraz',   'rodrigo.fuentes@gamedev.io',     '5567890123',  '$2a$10$dummyhash6', 'CLIENT', NULL, NULL, NULL, NULL),
('Lucía',     'Paredes Ibáñez',    'lucia.paredes@arquitecta.mx',    '5578901234',  '$2a$10$dummyhash7', 'CLIENT', NULL, NULL, NULL, NULL);