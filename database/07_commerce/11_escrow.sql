DROP TABLE IF EXISTS escrow CASCADE;

CREATE TABLE escrow (

    id_escrow BIGSERIAL PRIMARY KEY,

    id_paiement_sous_commande BIGINT NOT NULL,

    montant NUMERIC(12,2) NOT NULL,

    statut statut_escrow DEFAULT 'EN_ATTENTE',

    date_blocage TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    date_liberation TIMESTAMP,

    motif TEXT,

    CONSTRAINT fk_escrow_psc
        FOREIGN KEY(id_paiement_sous_commande)
        REFERENCES paiement_sous_commande(id_paiement_sous_commande)

);