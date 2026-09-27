DROP TABLE IF EXISTS localisation CASCADE;

CREATE TABLE localisation (

    id_localisation BIGSERIAL PRIMARY KEY,

    pays VARCHAR(100),

    region VARCHAR(100),

    departement VARCHAR(100),

    commune VARCHAR(100),

    arrondissement VARCHAR(100),

    quartier VARCHAR(100),

    adresse_complete TEXT,

    code_postal VARCHAR(20),

    latitude NUMERIC(10,7) NOT NULL,

    longitude NUMERIC(10,7) NOT NULL,

    altitude NUMERIC(8,2),

    precision_gps NUMERIC(5,2),

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);