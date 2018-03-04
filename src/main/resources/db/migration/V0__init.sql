DROP TABLE IF EXISTS app_user;
DROP SEQUENCE IF EXISTS app_user_id_seq;
CREATE SEQUENCE app_user_id_seq;
CREATE TABLE app_user (
  id                        BIGINT DEFAULT NEXTVAL('app_user_id_seq'),
  code                      VARCHAR(36) UNIQUE  NOT NULL,
  market_account_identifier VARCHAR(255) UNIQUE NOT NULL,
  active                    BOOLEAN             NOT NULL,
  subscription_type         VARCHAR(50)         NOT NULL,
  PRIMARY KEY (id)
);
CREATE INDEX app_user_code_uq ON app_user (code);
CREATE INDEX app_user_market_account_identifier_uq ON app_user (market_account_identifier);
