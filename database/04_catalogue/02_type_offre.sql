DROP TABLE IF EXISTS type_offre CASCADE;

CREATE TABLE type_offre (

    id_type_offre BIGSERIAL PRIMARY KEY,

    id_categorie BIGINT NOT NULL,

    libelle VARCHAR(120) NOT NULL,

    description TEXT,

    principale type_offre_principale NOT NULL DEFAULT 'PRODUIT',

    actif BOOLEAN DEFAULT TRUE,

    CONSTRAINT fk_typeoffre_categorie
        FOREIGN KEY(id_categorie)
        REFERENCES categorie(id_categorie)
        ON DELETE CASCADE

);