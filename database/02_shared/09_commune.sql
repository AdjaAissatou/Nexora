DROP TABLE IF EXISTS commune CASCADE;

CREATE TABLE commune (

    id_commune BIGSERIAL PRIMARY KEY,

    id_departement BIGINT NOT NULL,

    nom VARCHAR(150) NOT NULL,

    CONSTRAINT fk_commune_departement
        FOREIGN KEY(id_departement)
        REFERENCES departement(id_departement)
        ON DELETE CASCADE,

    CONSTRAINT uq_commune_departement_nom UNIQUE(id_departement, nom)

);
