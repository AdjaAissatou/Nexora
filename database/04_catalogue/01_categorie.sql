DROP TABLE IF EXISTS categorie CASCADE;

CREATE TABLE categorie (

    id_categorie BIGSERIAL PRIMARY KEY,

    id_categorie_parent BIGINT,

    nom VARCHAR(120) NOT NULL UNIQUE,

    description TEXT,

    icone VARCHAR(150),

    couleur VARCHAR(30),

    image VARCHAR(255),

    ordre_affichage INTEGER DEFAULT 0,

    actif BOOLEAN DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_categorie_parent
        FOREIGN KEY(id_categorie_parent)
        REFERENCES categorie(id_categorie)
        ON DELETE CASCADE

);