-- ============================================================
--  DDL - Schema (MariaDB)
--	Axel Fernando Montiel Aviles
-- ============================================================

CREATE DATABASE IF NOT EXISTS sispro3d_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE sispro3d_db;

-- ------------------------------------------------------------
--  ACCOUNT
-- ------------------------------------------------------------

CREATE TABLE account (
    id_user          INT                               NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(50)                       NOT NULL,
    lastName         VARCHAR(50)                       NOT NULL,
    email            VARCHAR(50)                       NOT NULL UNIQUE,
    phone            VARCHAR(15)                       NOT NULL UNIQUE,
    password         VARCHAR(255)                      NOT NULL,
    role             ENUM('ADMIN','CLIENT','EXPERT')   NOT NULL,
    specialty        VARCHAR(100)                      NULL,
    portfolio_url    VARCHAR(255)                      NULL,
    bio              TEXT                              NULL,
    years_experience INT                               NULL,
    created_at       TIMESTAMP                         DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
--  CATEGORY
-- ------------------------------------------------------------

CREATE TABLE category (
    id          INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    description TEXT
);

-- ------------------------------------------------------------
--  SERVICE
-- ------------------------------------------------------------

CREATE TABLE service (
    id                 INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title              VARCHAR(255)  NOT NULL,
    description        TEXT          NOT NULL,
    base_price         DECIMAL(10,2) NOT NULL,
    id_admin           INT,                       -- NULL mientras no se aprueba
    id_expert          INT           NOT NULL,
    category_id        INT           NOT NULL,
    created_at         TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP     ON UPDATE CURRENT_TIMESTAMP,
    delivery_time_days INT,
    CONSTRAINT fk_service_admin
        FOREIGN KEY (id_admin) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_service_expert
        FOREIGN KEY (id_expert) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_service_category
        FOREIGN KEY (category_id) REFERENCES category(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  REVIEW
-- ------------------------------------------------------------

CREATE TABLE review (
    id         INT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rating     INT       NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    id_client  INT       NOT NULL,
    id_service INT       NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_review_client_service UNIQUE (id_client, id_service),
    CONSTRAINT fk_review_client
        FOREIGN KEY (id_client) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_review_service
        FOREIGN KEY (id_service) REFERENCES service(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  QUOTE
-- ------------------------------------------------------------

CREATE TABLE quote (
    id           INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    status       ENUM('PENDING','ACCEPTED','REJECTED','EXPIRED') NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL,
    valid_until  DATE,
    description  TEXT,
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    id_client    INT           NOT NULL,
    id_service   INT           NOT NULL,
    CONSTRAINT fk_quote_client
        FOREIGN KEY (id_client) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_quote_service
        FOREIGN KEY (id_service) REFERENCES service(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  WORK ORDER
-- ------------------------------------------------------------

CREATE TABLE work_order (
    id           INT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    status       ENUM('PENDING','IN_PROGRESS','IN_REVIEW','COMPLETED','CANCELED') NOT NULL DEFAULT 'PENDING',
    started_at   TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_quote     INT       NOT NULL UNIQUE,
    CONSTRAINT fk_work_order_quote
        FOREIGN KEY (id_quote) REFERENCES quote(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  DELIVERABLE
-- ------------------------------------------------------------

CREATE TABLE deliverable (
    id         INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(60)  NOT NULL,
    url_file   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_type  VARCHAR(50)  NOT NULL,
    id_order   INT          NOT NULL,
    CONSTRAINT fk_deliverable_order
        FOREIGN KEY (id_order) REFERENCES work_order(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  PREVIEW
-- ------------------------------------------------------------

CREATE TABLE preview (
    id             INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    caption        VARCHAR(255) NOT NULL,
    url_file       VARCHAR(255) NOT NULL,
    deliverable_id INT          NOT NULL,
    CONSTRAINT fk_preview_deliverable
        FOREIGN KEY (deliverable_id) REFERENCES deliverable(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  THREAD
-- ------------------------------------------------------------

CREATE TABLE thread (
    id       INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_order INT NOT NULL UNIQUE,
    CONSTRAINT fk_thread_order
        FOREIGN KEY (id_order) REFERENCES work_order(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  MESSAGE
-- ------------------------------------------------------------

CREATE TABLE message (
    id         INT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_thread  INT       NOT NULL,
    user_id    INT       NOT NULL,
    content    TEXT      NOT NULL,
    time_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_thread
        FOREIGN KEY (id_thread) REFERENCES thread(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_message_account
        FOREIGN KEY (user_id) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE CASCADE
);




-- ============================================================
--  DUMMY DATA - Plataforma de Servicios de Modelado 3D
-- ============================================================

-- ------------------------------------------------------------
--  ACCOUNT
-- ------------------------------------------------------------

INSERT INTO account (name, lastName, email, phone, password, role, specialty, portfolio_url, bio, years_experience) VALUES
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
('Modelado de personajes',   'Creación de personajes 3D orgánicos o estilizados para juegos, películas o ilustración'),
('Modelado de entornos',     'Diseño y construcción de escenarios, props y arquitectura en 3D'),
('Texturizado',              'Creación de materiales, mapas UV y texturas PBR para modelos existentes'),
('Animación',                'Rigging, skinning y animación de personajes o elementos de escena'),
('Renderizado',              'Configuración de iluminación, shaders y render final de alta calidad');

-- ------------------------------------------------------------
--  SERVICE
-- ------------------------------------------------------------

INSERT INTO service (title, description, base_price, id_admin, id_expert, category_id, delivery_time_days) VALUES
('Modelado de personaje estilizado',
 'Creación de personaje 3D estilizado listo para videojuego. Incluye malla optimizada, UVs y exportación en FBX/OBJ.',
 9500.00, 1, 2, 1, 20),

('Sculpt de personaje realista',
 'Escultura digital de alta resolución en ZBrush. Incluye retopología y mapas de desplazamiento.',
 18000.00, 1, 2, 1, 30),

('Texturizado PBR completo',
 'Texturizado realista con Substance Painter. Entrega de mapas Albedo, Normal, Roughness, Metallic y AO.',
 6500.00, 1, 3, 3, 10),

('Texturizado estilizado hand-painted',
 'Estilo hand-painted para modelos de juego. Inspirado en estilos tipo World of Warcraft o Fortnite.',
 5000.00, 1, 3, 3, 12),

('Rigging y skinning de personaje',
 'Configuración de esqueleto, pesos de skinning y controles de animación para personaje bípedo.',
 7500.00, 1, 4, 4, 15),

('Animación de ciclo de movimiento',
 'Paquete de animaciones: idle, caminar, correr, saltar y atacar. Entrega en FBX con 30fps.',
 12000.00, 1, 4, 4, 25),

('Modelado de prop arquitectónico',
 'Modelado de mobiliario o elemento arquitectónico para visualización. Incluye texturizado básico.',
 4200.00, NULL, 3, 2, 10);

-- ------------------------------------------------------------
--  REVIEW
-- ------------------------------------------------------------

INSERT INTO review (rating, comment, id_client, id_service) VALUES
(5, 'Emilio entregó un personaje increíble, superó todas mis expectativas. El nivel de detalle es impresionante.',  5, 1),
(5, 'Las texturas de Daniela le dieron vida al modelo. Muy profesional y puntual con los tiempos.',                 6, 3),
(4, 'Buen trabajo de rigging, los controles son intuitivos. Le faltó un poco más de detalle en los dedos.',         6, 5),
(5, 'Las animaciones quedaron fluidas y naturales. Óscar entiende muy bien el peso y la física del personaje.',     7, 6);

-- ------------------------------------------------------------
--  QUOTE
-- ------------------------------------------------------------

INSERT INTO quote (status, total_amount, valid_until, description, id_client, id_service) VALUES
('ACCEPTED',  9500.00,  '2025-06-10', 'Personaje estilizado para juego mobile, estilo cartoon. Máximo 5k polígonos.',                              5, 1),
('ACCEPTED',  6500.00,  '2025-06-20', 'Texturizado PBR para nave espacial ya modelada. Resolución de mapas 4096x4096.',                            6, 3),
('ACCEPTED',  7500.00,  '2025-07-01', 'Rigging para personaje femenino bípedo. Debe ser compatible con Unreal Engine 5.',                          6, 5),
('ACCEPTED',  12000.00, '2025-07-15', 'Pack de 6 animaciones para personaje de juego de acción. Incluye animaciones de combate.',                   7, 6),
('PENDING',   18000.00, '2025-08-01', 'Sculpt realista de criatura fantástica para cortometraje. Se requiere versión de alta y baja resolución.',   5, 2),
('REJECTED',  4200.00,  '2025-05-15', 'Prop de escritorio moderno para visualización arquitectónica. Cliente optó por asset de tienda.',            7, 7);

-- ------------------------------------------------------------
--  WORK ORDER
-- ------------------------------------------------------------

INSERT INTO work_order (status, started_at, completed_at, id_quote) VALUES
('COMPLETED',   '2025-04-01 09:00:00', '2025-04-19 17:00:00', 1),
('COMPLETED',   '2025-04-10 10:00:00', '2025-04-19 16:00:00', 2),
('IN_PROGRESS', '2025-05-05 09:00:00', NULL,                   3),
('IN_REVIEW',   '2025-04-20 08:00:00', NULL,                   4),
('PENDING',     NULL,                  NULL,                   5);

-- ------------------------------------------------------------
--  DELIVERABLE
-- ------------------------------------------------------------

-- Orden 1: Personaje estilizado completado
INSERT INTO deliverable (name, url_file, file_type, id_order) VALUES
('Modelo FBX - personaje principal',     'https://files.render3d.mx/orden1/personaje.fbx',           'model/fbx',       1),
('Modelo OBJ - personaje principal',     'https://files.render3d.mx/orden1/personaje.obj',           'model/obj',       1),
('Texturas empaquetadas',                'https://files.render3d.mx/orden1/texturas.zip',            'application/zip', 1),
('Renders de presentación',              'https://files.render3d.mx/orden1/renders.zip',             'application/zip', 1);

-- Orden 2: Texturizado completado
INSERT INTO deliverable (name, url_file, file_type, id_order) VALUES
('Mapa Albedo 4K',                       'https://files.render3d.mx/orden2/albedo_4k.png',           'image/png',       2),
('Mapa Normal 4K',                       'https://files.render3d.mx/orden2/normal_4k.png',           'image/png',       2),
('Mapa Roughness-Metallic 4K',           'https://files.render3d.mx/orden2/roughness_metallic.png',  'image/png',       2),
('Proyecto Substance Painter',           'https://files.render3d.mx/orden2/nave.spp',                'application/spp', 2);

-- ------------------------------------------------------------
--  PREVIEW
-- ------------------------------------------------------------

INSERT INTO preview (caption, url_file, deliverable_id) VALUES
('Vista frontal del personaje',          'https://files.render3d.mx/orden1/preview-frente.png',      1),
('Vista lateral del personaje',          'https://files.render3d.mx/orden1/preview-lateral.png',     1),
('Vista wireframe',                      'https://files.render3d.mx/orden1/preview-wireframe.png',   1),
('Preview albedo sobre modelo',          'https://files.render3d.mx/orden2/preview-albedo.png',      5),
('Preview render final con iluminación', 'https://files.render3d.mx/orden2/preview-render.png',      5);

-- ------------------------------------------------------------
--  THREAD
-- ------------------------------------------------------------

INSERT INTO thread (id_order) VALUES (1), (2), (3), (4), (5);

-- ------------------------------------------------------------
--  MESSAGE
-- ------------------------------------------------------------

INSERT INTO message (id_thread, user_id, content) VALUES
-- Hilo orden 1: Personaje estilizado (completada)
(1, 5, 'Hola Emilio, acabo de revisar el modelo y quedó espectacular. El nivel de detalle en la cara superó lo que esperaba.'),
(1, 2, 'Gracias Mariana! Me alegra mucho. Intenté mantener las proporciones que me indicaste en el brief. ¿Algún ajuste antes de cerrar?'),
(1, 5, 'Ninguno, todo perfecto. Puedes marcar la orden como completada.'),

-- Hilo orden 2: Texturizado (completada)
(2, 6, 'Daniela, los mapas se ven increíbles en el motor. La nave tiene exactamente el look sucio y desgastado que necesitábamos.'),
(2, 3, 'Me alegra que funcione bien en Unreal. Usé capas de desgaste procedural en Substance para que se vea más orgánico. Cualquier variación de color me avisas.'),
(2, 6, 'Perfecto, así lo dejaré. Muchas gracias!'),

-- Hilo orden 3: Rigging en progreso
(3, 6, 'Óscar, ¿cómo va el rigging? ¿Ya probaste los controles de la mano?'),
(3, 4, 'Hola Rodrigo, sí, los dedos ya tienen controles individuales y un atributo de puño para animarlos rápido. Esta semana termino los controles faciales básicos.'),
(3, 6, 'Excelente, me alegra. ¿Crees que tenga problema al importarlo a Unreal 5?'),
(3, 4, 'No debería, estoy nombrando los huesos con la convención de UE5 desde el inicio para evitar problemas.'),

-- Hilo orden 4: Animaciones en revisión
(4, 7, 'Lucía, ya subí el paquete de animaciones para que lo revises. El ciclo de carrera lo ajusté para que se vea más pesado, como pediste.'),
(4, 7, 'Acabo de verlas, el idle y el caminar se ven muy naturales. El salto me parece un poco rápido en la fase de caída, ¿puedes alargarlo?'),
(4, 4, 'Claro, le agrego unos frames más a la anticipación de aterrizaje. Lo tengo listo mañana.'),

-- Hilo orden 5: Sculpt pendiente
(5, 5, 'Buenos días Ricardo, ya fue aprobada mi cotización. ¿Cuándo comenzamos con el sculpt de la criatura?'),
(5, 2, 'Hola Mariana, esta semana reviso el brief a detalle y el lunes te mando las primeras exploraciones de silueta para que apruebes la dirección antes de esculpir.');



