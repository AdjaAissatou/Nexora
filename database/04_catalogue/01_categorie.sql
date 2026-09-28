DROP TABLE IF EXISTS categorie CASCADE;

CREATE TABLE categorie (

    id_categorie BIGSERIAL PRIMARY KEY,

    id_categorie_parent BIGINT,

    nom VARCHAR(120) NOT NULL,

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

-- Unicité par branche, pas globale : deux domaines différents peuvent chacun avoir
-- une sous-catégorie "Services administratifs" ou "Conseil" sans entrer en conflit.
CREATE UNIQUE INDEX uq_categorie_parent_nom ON categorie ((COALESCE(id_categorie_parent, 0)), nom);