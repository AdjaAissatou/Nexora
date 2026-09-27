DROP TABLE IF EXISTS type_espace CASCADE;

CREATE TABLE type_espace (

    id_type_espace BIGSERIAL PRIMARY KEY,

    nom VARCHAR(100) NOT NULL UNIQUE,

    description TEXT,

    icone VARCHAR(100),

    couleur VARCHAR(20),

    ordre_affichage INTEGER DEFAULT 0,

    actif BOOLEAN DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);