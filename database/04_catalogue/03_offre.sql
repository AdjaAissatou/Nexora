DROP TABLE IF EXISTS offre CASCADE;

CREATE TABLE offre (

    id_offre BIGSERIAL PRIMARY KEY,

    id_espace BIGINT NOT NULL,

    id_type_offre BIGINT NOT NULL,

    id_categorie BIGINT NOT NULL,

    titre VARCHAR(200) NOT NULL,

    description TEXT,

    prix NUMERIC(12,2),

    ancien_prix NUMERIC(12,2),

    negociable BOOLEAN DEFAULT FALSE,

    disponible BOOLEAN DEFAULT TRUE,

    est_commandable BOOLEAN DEFAULT TRUE,

    est_reservable BOOLEAN DEFAULT FALSE,

    stockable BOOLEAN DEFAULT FALSE,

    quantite_disponible INTEGER,

    necessite_validation BOOLEAN DEFAULT FALSE,

    vue_count BIGINT DEFAULT 0,

    score_pertinence DOUBLE PRECISION DEFAULT 0,

    statut statut_offre DEFAULT 'BROUILLON',

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    date_modification TIMESTAMP,

    date_publication TIMESTAMP,

    CONSTRAINT fk_offre_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)
        ON DELETE CASCADE,

    CONSTRAINT fk_offre_type
        FOREIGN KEY(id_type_offre)
        REFERENCES type_offre(id_type_offre),

    CONSTRAINT fk_offre_categorie
        FOREIGN KEY(id_categorie)
        REFERENCES categorie(id_categorie)

);