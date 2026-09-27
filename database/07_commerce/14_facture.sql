DROP TABLE IF EXISTS facture CASCADE;

CREATE TABLE facture (

    id_facture BIGSERIAL PRIMARY KEY,

    id_commande BIGINT NOT NULL,

    numero VARCHAR(50) UNIQUE,

    montant NUMERIC(12,2),

    date_emission TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    statut statut_facture DEFAULT 'EMISE',

    CONSTRAINT fk_facture_commande
        FOREIGN KEY(id_commande)
        REFERENCES commande(id_commande)
        ON DELETE CASCADE

);