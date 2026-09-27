DROP TABLE IF EXISTS ligne_commande CASCADE;

CREATE TABLE ligne_commande (

    id_ligne_commande BIGSERIAL PRIMARY KEY,

    id_sous_commande BIGINT NOT NULL,

    id_offre BIGINT NOT NULL,

    quantite INTEGER DEFAULT 1,

    prix_unitaire NUMERIC(12,2) NOT NULL,

    remise NUMERIC(12,2) DEFAULT 0,

    montant NUMERIC(12,2) NOT NULL,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ligne_souscommande
        FOREIGN KEY(id_sous_commande)
        REFERENCES sous_commande(id_sous_commande)
        ON DELETE CASCADE,

    CONSTRAINT fk_ligne_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)

);