DROP TABLE IF EXISTS produit CASCADE;

CREATE TABLE produit (

    id_produit BIGSERIAL PRIMARY KEY,

    id_offre BIGINT UNIQUE NOT NULL,

    marque VARCHAR(120),

    modele VARCHAR(120),

    reference VARCHAR(120),

    quantite_stock INTEGER DEFAULT 0,

    poids DOUBLE PRECISION,

    garantie VARCHAR(120),

    neuf BOOLEAN DEFAULT TRUE,

    CONSTRAINT fk_produit_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)

        ON DELETE CASCADE

);