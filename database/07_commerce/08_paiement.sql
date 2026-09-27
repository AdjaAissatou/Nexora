DROP TABLE IF EXISTS paiement CASCADE;

CREATE TABLE paiement (

    id_paiement BIGSERIAL PRIMARY KEY,

    id_commande BIGINT NOT NULL,

    reference VARCHAR(100),

    montant NUMERIC(12,2) NOT NULL,

    mode_paiement mode_paiement,

    statut statut_paiement DEFAULT 'EN_ATTENTE',

    fournisseur_paiement VARCHAR(100),

    date_paiement TIMESTAMP,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_paiement_commande
        FOREIGN KEY(id_commande)
        REFERENCES commande(id_commande)

);