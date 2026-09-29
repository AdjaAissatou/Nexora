DROP TABLE IF EXISTS lieu_public CASCADE;

CREATE TABLE lieu_public (

    id_lieu BIGSERIAL PRIMARY KEY,

    nom VARCHAR(200) NOT NULL,

    type_lieu VARCHAR(50) NOT NULL,

    region VARCHAR(100),

    departement VARCHAR(100),

    commune VARCHAR(100),

    adresse_complete TEXT,

    latitude NUMERIC(10,7) NOT NULL,

    longitude NUMERIC(10,7) NOT NULL,

    description TEXT,

    actif BOOLEAN NOT NULL DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);

CREATE INDEX idx_lieu_public_nom ON lieu_public USING gin (nom gin_trgm_ops);
CREATE INDEX idx_lieu_public_type ON lieu_public(type_lieu);
CREATE INDEX idx_lieu_public_commune ON lieu_public(commune);
