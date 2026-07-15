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

-- ------------------------------------------------------------
--  CATEGORY
-- ------------------------------------------------------------
INSERT INTO category (name, description) VALUES
('Modelado 3D',       'Creación de modelos tridimensionales de personajes, objetos y escenarios'),
('Texturizado',       'Aplicación de texturas PBR, materiales y lookdev'),
('Animación',         'Animación de personajes, props y cámaras'),
('Rigging',           'Construcción de esqueletos y sistemas de control para animación'),
('Iluminación',       'Configuración de luces y render para escenas'),
('VFX',               'Efectos visuales: partículas, simulaciones, composición');

-- ------------------------------------------------------------
--  OFFERED_SERVICE
-- ------------------------------------------------------------
INSERT INTO offered_service (title, description, base_price, id_expert, id_admin, id_category, status, delivery_time_days, created_at, updated_at) VALUES
-- Emilio (expert id=2) — Modelado
('Personaje para videojuego',    'Modelado completo de personaje stylizado, incluye low-poly y high-poly.',                    3500.00, 2, 1, 1, 'APPROVED',  14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Criatura orgánica',            'Modelado de criatura fantasy con referencia o concept art.',                               2800.00, 2, NULL, 1, 'PENDING',   10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Daniela (expert id=3) — Texturizado
('Texturizado PBR realista',     'Texturizado completo en Substance Painter con mapas albedo, normal, roughness y AO.',       2200.00, 3, 1, 2, 'APPROVED',   7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Material tileable',            'Creación de materiales tileable para arquitectura o props.',                               1500.00, 3, NULL, 2, 'PENDING',    5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Óscar (expert id=4) — Animación y rigging
('Animación de walk cycle',      'Animación cíclica de caminata para personaje humanoid.',                                   1800.00, 4, 1, 3, 'APPROVED',   5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Rig completo con facial',      'Rigging con control facial, IK/FK switch y setup para Motor.',                             4000.00, 4, NULL, 4, 'PENDING',   21, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Animación de ataque',          'Animación de combo de 3 golpes para personaje de acción.',                                 2500.00, 4, 1, 3, 'APPROVED',   7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);