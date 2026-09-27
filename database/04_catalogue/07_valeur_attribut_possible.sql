DROP TABLE IF EXISTS valeur_attribut_possible CASCADE;

CREATE TABLE valeur_attribut_possible (

    id_valeur BIGSERIAL PRIMARY KEY,

    id_attribut BIGINT NOT NULL,

    valeur VARCHAR(255) NOT NULL,

    ordre_affichage INTEGER DEFAULT 0,

    actif BOOLEAN DEFAULT TRUE,

    CONSTRAINT fk_valeur_attribut
        FOREIGN KEY(id_attribut)
        REFERENCES attribut(id_attribut)
        ON DELETE CASCADE

);