DROP TABLE IF EXISTS adresse CASCADE;

CREATE TABLE adresse (

    id_adresse BIGSERIAL PRIMARY KEY,

    id_espace BIGINT NOT NULL,

    pays VARCHAR(100),

    region VARCHAR(100),

    departement VARCHAR(100),

    commune VARCHAR(100),

    arrondissement VARCHAR(100),

    quartier VARCHAR(100),

    adresse_complete TEXT,

    code_postal VARCHAR(30),

    latitude NUMERIC(10,7),

    longitude NUMERIC(10,7),

    precision_gps VARCHAR(255),

    principale BOOLEAN DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_adresse_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)

        ON DELETE CASCADE

);