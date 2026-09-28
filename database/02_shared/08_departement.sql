DROP TABLE IF EXISTS departement CASCADE;

CREATE TABLE departement (

    id_departement BIGSERIAL PRIMARY KEY,

    id_region BIGINT NOT NULL,

    nom VARCHAR(100) NOT NULL,

    CONSTRAINT fk_departement_region
        FOREIGN KEY(id_region)
        REFERENCES region(id_region)
        ON DELETE CASCADE,

    CONSTRAINT uq_departement_region_nom UNIQUE(id_region, nom)

);
