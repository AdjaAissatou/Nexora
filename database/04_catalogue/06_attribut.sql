DROP TABLE IF EXISTS attribut CASCADE;

CREATE TABLE attribut (

    id_attribut BIGSERIAL PRIMARY KEY,

    id_categorie BIGINT NOT NULL,

    nom VARCHAR(150) NOT NULL,

    code VARCHAR(100) UNIQUE NOT NULL,

    type_champ type_champ NOT NULL,

    obligatoire BOOLEAN DEFAULT FALSE,

    filtrable BOOLEAN DEFAULT TRUE,

    affichable BOOLEAN DEFAULT TRUE,

    ordre_affichage INTEGER DEFAULT 0,

    unite VARCHAR(50),

    aide TEXT,

    actif BOOLEAN DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attribut_categorie
        FOREIGN KEY(id_categorie)
        REFERENCES categorie(id_categorie)
        ON DELETE CASCADE

);