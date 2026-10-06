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

    -- Produits seulement (taille, couleur, garantie…), services seulement, ou les deux
    pour_type VARCHAR(10) NOT NULL DEFAULT 'TOUS',

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_attribut_pour_type CHECK (pour_type IN ('TOUS', 'PRODUIT', 'SERVICE')),

    CONSTRAINT fk_attribut_categorie
        FOREIGN KEY(id_categorie)
        REFERENCES categorie(id_categorie)
        ON DELETE CASCADE

);