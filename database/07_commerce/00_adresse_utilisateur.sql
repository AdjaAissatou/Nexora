DROP TABLE IF EXISTS adresse_utilisateur CASCADE;

CREATE TABLE adresse_utilisateur (

    id_adresse_utilisateur BIGSERIAL PRIMARY KEY,

    id_utilisateur BIGINT NOT NULL,

    libelle VARCHAR(100) NOT NULL,

    destinataire VARCHAR(150),

    telephone VARCHAR(30),

    pays VARCHAR(100) DEFAULT 'Sénégal',

    region VARCHAR(100),

    departement VARCHAR(100),

    commune VARCHAR(100),

    quartier VARCHAR(150),

    adresse_complete TEXT NOT NULL,

    code_postal VARCHAR(20),

    latitude NUMERIC(10,7),

    longitude NUMERIC(10,7),

    principale BOOLEAN DEFAULT FALSE,

    instructions_livraison TEXT,

    actif BOOLEAN DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_adresse_user
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur)
        ON DELETE CASCADE

);