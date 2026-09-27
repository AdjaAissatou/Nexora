DROP TABLE IF EXISTS wallet CASCADE;

CREATE TABLE wallet (

    id_wallet BIGSERIAL PRIMARY KEY,

    id_utilisateur BIGINT NOT NULL UNIQUE,

    solde NUMERIC(12,2) DEFAULT 0,

    actif BOOLEAN DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_utilisateur
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur)

);