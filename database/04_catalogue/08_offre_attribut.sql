DROP TABLE IF EXISTS offre_attribut CASCADE;

CREATE TABLE offre_attribut (

    id_offre_attribut BIGSERIAL PRIMARY KEY,

    id_offre BIGINT NOT NULL,

    id_attribut BIGINT NOT NULL,

    valeur_texte TEXT,

    valeur_nombre NUMERIC(18,2),

    valeur_date DATE,

    id_valeur BIGINT,

    -- Taille, couleur… proposée mais momentanément épuisée : affichée barrée, non commandable
    epuise BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_offreattribut_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)
        ON DELETE CASCADE,

    CONSTRAINT fk_offreattribut_attribut
        FOREIGN KEY(id_attribut)
        REFERENCES attribut(id_attribut),

    CONSTRAINT fk_offreattribut_valeur
        FOREIGN KEY(id_valeur)
        REFERENCES valeur_attribut_possible(id_valeur)

);