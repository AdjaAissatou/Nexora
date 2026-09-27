DROP TABLE IF EXISTS panier CASCADE;

CREATE TABLE panier (

    id_panier BIGSERIAL PRIMARY KEY,

    id_utilisateur BIGINT NOT NULL,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    date_modification TIMESTAMP,

    actif BOOLEAN DEFAULT TRUE,

    CONSTRAINT fk_panier_utilisateur
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur)
        ON DELETE CASCADE

);