CREATE SEQUENCE apporg_id_seq;
CREATE TABLE apporg (
  id                        BIGINT DEFAULT NEXTVAL('apporg_id_seq'),
  code                      VARCHAR(36) UNIQUE  NOT NULL,
  market_identifier         VARCHAR(255) UNIQUE NOT NULL,
  active                    BOOLEAN             NOT NULL,
  max_users                 BIGINT,

  name                      VARCHAR(255),
  country                   VARCHAR(2),

  PRIMARY KEY (id)
);
CREATE INDEX apporg_code_idx ON apporg (code);
CREATE INDEX apporg_market_identifier_idx ON apporg (market_identifier);
CREATE INDEX apporg_country_idx ON apporg (country);

CREATE SEQUENCE appuser_id_seq;
CREATE TABLE appuser (
  id                        BIGINT DEFAULT NEXTVAL('appuser_id_seq'),
  code                      VARCHAR(36) UNIQUE  NOT NULL,
  market_identifier         VARCHAR(255)        NOT NULL,

  first_name                VARCHAR(255),
  last_name                 VARCHAR(255),
  open_id                   VARCHAR(255)        NOT NULL,

  apporg_id                 BIGINT              NOT NULL,

  PRIMARY KEY (id)
);
CREATE INDEX appuser_code_idx ON appuser (code);
CREATE INDEX appuser_market_identifier_idx ON appuser (market_identifier);
ALTER TABLE appuser ADD CONSTRAINT appuser_apporg_id_fk FOREIGN KEY (apporg_id) REFERENCES apporg (id);
ALTER TABLE appuser ADD CONSTRAINT appuser_market_identifier_apporg_id_uq UNIQUE (market_identifier, apporg_id);
