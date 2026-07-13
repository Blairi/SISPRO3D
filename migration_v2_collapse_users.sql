-- ============================================================
--  Migration v2: Collapse Admin/Expert/Client into Account
--  - Add `role` column (replaces `type`)
--  - Add expert-specific fields (nullable)
--  - Drop admin, expert, client tables
--  - Update FK references
-- ============================================================

USE sispro3d_db;

-- 1. Add role column and expert fields to account
ALTER TABLE account
    CHANGE COLUMN type role ENUM('ADMIN','CLIENT','EXPERT') NOT NULL,
    ADD COLUMN specialty        VARCHAR(100) NULL AFTER role,
    ADD COLUMN portfolio_url    VARCHAR(255) NULL AFTER specialty,
    ADD COLUMN bio              TEXT         NULL AFTER portfolio_url,
    ADD COLUMN years_experience INT          NULL AFTER bio;

-- 2. Migrate expert data to account
UPDATE account a
JOIN expert e ON a.id_user = e.id_user
SET a.specialty        = e.specialty,
    a.portfolio_url    = e.portfolio_url,
    a.bio              = e.bio,
    a.years_experience = e.years_experience;

-- 3. Drop FK constraints referencing admin/expert/client
ALTER TABLE service  DROP FOREIGN KEY fk_service_admin;
ALTER TABLE service  DROP FOREIGN KEY fk_service_expert;
ALTER TABLE review   DROP FOREIGN KEY fk_review_client;
ALTER TABLE quote    DROP FOREIGN KEY fk_quote_client;

-- 4. Drop the now-redundant tables
DROP TABLE admin;
DROP TABLE expert;
DROP TABLE client;

-- 5. Recreate FK constraints pointing directly to account
ALTER TABLE service
    ADD CONSTRAINT fk_service_admin
        FOREIGN KEY (id_admin) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE SET NULL,
    ADD CONSTRAINT fk_service_expert
        FOREIGN KEY (id_expert) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE review
    ADD CONSTRAINT fk_review_client
        FOREIGN KEY (id_client) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE quote
    ADD CONSTRAINT fk_quote_client
        FOREIGN KEY (id_client) REFERENCES account(id_user)
        ON UPDATE CASCADE ON DELETE CASCADE;
