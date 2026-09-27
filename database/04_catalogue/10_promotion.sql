DROP TABLE IF EXISTS promotion CASCADE;

CREATE TABLE promotion (

    id_promotion BIGSERIAL PRIMARY KEY,

    id_offre BIGINT,

    id_espace BIGINT,

    code VARCHAR(50),

    nom VARCHAR(150) NOT NULL,

    description TEXT,

    priorite INTEGER DEFAULT 0,

    type_reduction type_reduction NOT NULL,

    valeur NUMERIC(10,2) NOT NULL,

    montant_minimum NUMERIC(10,2),

    date_debut TIMESTAMP NOT NULL,

    date_fin TIMESTAMP NOT NULL,

    actif BOOLEAN DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_promotion_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)
        ON DELETE CASCADE,

    CONSTRAINT fk_promotion_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)
        ON DELETE CASCADE,

    CONSTRAINT chk_promotion
        CHECK (
            id_offre IS NOT NULL
            OR id_espace IS NOT NULL
        )

);