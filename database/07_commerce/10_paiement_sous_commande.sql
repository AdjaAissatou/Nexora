DROP TABLE IF EXISTS paiement_sous_commande CASCADE;

CREATE TABLE paiement_sous_commande (

    id_paiement_sous_commande BIGSERIAL PRIMARY KEY,

    id_paiement BIGINT NOT NULL,

    id_sous_commande BIGINT NOT NULL,

    montant NUMERIC(12,2) NOT NULL,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_psc_paiement
        FOREIGN KEY(id_paiement)
        REFERENCES paiement(id_paiement)
        ON DELETE CASCADE,

    CONSTRAINT fk_psc_sous_commande
        FOREIGN KEY(id_sous_commande)
        REFERENCES sous_commande(id_sous_commande)
        ON DELETE CASCADE,

    CONSTRAINT uk_paiement_sous_commande
        UNIQUE(id_paiement, id_sous_commande)

);