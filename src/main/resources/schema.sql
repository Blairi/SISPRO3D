-- ============================================================
--  DDL - Schema (MariaDB)
--  Axelfernando Montiel Aviles
-- ============================================================

CREATE DATABASE IF NOT EXISTS sispro3d_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE sispro3d_db;

-- ------------------------------------------------------------
--  ACCOUNT
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS account (
    id_user          BIGINT                              NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(50)                         NOT NULL,
    last_name        VARCHAR(50)                         NOT NULL,
    email            VARCHAR(50)                         NOT NULL UNIQUE,
    phone            VARCHAR(20)                         NULL,
    password         VARCHAR(255)                        NOT NULL,
    role             ENUM('ADMIN','CLIENT','EXPERT')     NOT NULL,
    specialty        VARCHAR(100)                        NULL,
    portfolio_url    VARCHAR(255)                        NULL,
    bio              TEXT                                NULL,
    years_experience INT                                 NULL,
    created_at       TIMESTAMP                           DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
--  CATEGORY
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS category (
    id_category  BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(50) NOT NULL UNIQUE,
    description  TEXT
);

-- ------------------------------------------------------------
--  OFFERED_SERVICE
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS offered_service (
    id_offered_service  BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(255)    NOT NULL,
    description         TEXT,
    base_price          DECIMAL(10,2)   NOT NULL,
    id_admin            BIGINT,
    id_expert           BIGINT          NOT NULL,
    id_category         BIGINT          NOT NULL,
    status              ENUM('PENDING','APPROVED','REJECTED') NOT NULL,
    delivery_time_days  INT             NOT NULL,
    created_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NULL,
    CONSTRAINT fk_service_admin
        FOREIGN KEY (id_admin) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_service_expert
        FOREIGN KEY (id_expert) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_service_category
        FOREIGN KEY (id_category) REFERENCES category(id_category)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  REVIEW
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS review (
    id_review            BIGINT  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rating               INT     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment              TEXT,
    id_client            BIGINT  NOT NULL,
    id_offered_service   BIGINT  NOT NULL,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_review_client_service UNIQUE (id_client, id_offered_service),
    CONSTRAINT fk_review_client
        FOREIGN KEY (id_client) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_review_service
        FOREIGN KEY (id_offered_service) REFERENCES offered_service(id_offered_service)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  QUOTE
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS quote (
    id             BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    status         ENUM('PENDING','ACCEPTED','REJECTED','EXPIRED') NOT NULL DEFAULT 'PENDING',
    total_amount   DECIMAL(10,2)  NOT NULL,
    valid_until    DATE,
    description    TEXT,
    created_at     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    id_client      BIGINT         NOT NULL,
    id_service     BIGINT         NOT NULL,
    CONSTRAINT fk_quote_client
        FOREIGN KEY (id_client) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_quote_service
        FOREIGN KEY (id_service) REFERENCES offered_service(id_offered_service)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  WORK ORDER
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS work_order (
    id           BIGINT     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    status       ENUM('PENDING','IN_PROGRESS','IN_REVIEW','COMPLETED','CANCELED') NOT NULL DEFAULT 'PENDING',
    started_at   TIMESTAMP  NULL,
    completed_at TIMESTAMP  NULL,
    created_at   TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    id_quote     BIGINT     NOT NULL UNIQUE,
    CONSTRAINT fk_work_order_quote
        FOREIGN KEY (id_quote) REFERENCES quote(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  DELIVERABLE
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS deliverable (
    id         BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(60)    NOT NULL,
    url_file   VARCHAR(255)   NOT NULL,
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_type  VARCHAR(50)    NOT NULL,
    id_order   BIGINT         NOT NULL,
    CONSTRAINT fk_deliverable_order
        FOREIGN KEY (id_order) REFERENCES work_order(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  PREVIEW
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS preview (
    id             BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    caption        VARCHAR(255)   NOT NULL,
    url_file       VARCHAR(255)   NOT NULL,
    deliverable_id BIGINT         NOT NULL,
    CONSTRAINT fk_preview_deliverable
        FOREIGN KEY (deliverable_id) REFERENCES deliverable(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  THREAD
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS thread (
    id        BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_order  BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_thread_order
        FOREIGN KEY (id_order) REFERENCES work_order(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
--  MESSAGE
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS message (
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_thread  BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    content    TEXT        NOT NULL,
    time_stamp TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_thread
        FOREIGN KEY (id_thread) REFERENCES thread(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_message_account
        FOREIGN KEY (user_id) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE CASCADE
);
