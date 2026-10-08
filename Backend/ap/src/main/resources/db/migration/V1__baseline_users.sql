-- V1 — baseline schema.
--
-- This captures what Hibernate's `ddl-auto=update` had been generating
-- implicitly, so that `ddl-auto=validate` has something real to validate
-- against and the schema becomes a reviewable, versioned artifact.
--
-- From here on: schema changes are new V*__*.sql files. Never edit this one.

CREATE TABLE users (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    firstname VARCHAR(255) NULL,
    lastname  VARCHAR(255) NULL,
    username  VARCHAR(255) NOT NULL,
    email     VARCHAR(255) NULL,
    gender    VARCHAR(255) NULL,
    height    BIGINT       NULL,
    weight    BIGINT       NULL,
    password  VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Email is indexed but deliberately NOT unique yet: the registration form
-- treats email as optional, so every empty submission would send "" and
-- collide. Make it unique in a later migration, once server-side validation
-- rejects or normalises blank emails.

CREATE INDEX idx_users_email ON users (email);
