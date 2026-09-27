DROP TABLE IF EXISTS tag CASCADE;

CREATE TABLE tag (

    id_tag BIGSERIAL PRIMARY KEY,

    nom VARCHAR(100) NOT NULL UNIQUE,

    couleur VARCHAR(20),

    icone VARCHAR(100),

    description TEXT,

    actif BOOLEAN DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);