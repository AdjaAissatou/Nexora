DROP TABLE IF EXISTS ligne_panier CASCADE;

CREATE TABLE ligne_panier (

    id_ligne_panier BIGSERIAL PRIMARY KEY,

    id_panier BIGINT NOT NULL,

    id_offre BIGINT NOT NULL,

    quantite INTEGER DEFAULT 1,

    selectionnee BOOLEAN DEFAULT FALSE,

    date_selection TIMESTAMP,

    est_reservable BOOLEAN DEFAULT FALSE,

    prix_unitaire NUMERIC(12,2) NOT NULL,

    date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lp_panier
        FOREIGN KEY(id_panier)
        REFERENCES panier(id_panier)
        ON DELETE CASCADE,

    CONSTRAINT fk_lp_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)

);