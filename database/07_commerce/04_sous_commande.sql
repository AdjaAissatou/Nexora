DROP TABLE IF EXISTS sous_commande CASCADE;

CREATE TABLE sous_commande (

    id_sous_commande BIGSERIAL PRIMARY KEY,

    id_commande BIGINT NOT NULL,

    id_espace BIGINT NOT NULL,

    numero VARCHAR(60) UNIQUE NOT NULL,

    montant NUMERIC(12,2) DEFAULT 0,

    frais_livraison NUMERIC(12,2) DEFAULT 0,

    remise NUMERIC(12,2) DEFAULT 0,

    montant_final NUMERIC(12,2) DEFAULT 0,

    statut statut_commande DEFAULT 'EN_ATTENTE',

    commentaire TEXT,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_souscommande_commande
        FOREIGN KEY(id_commande)
        REFERENCES commande(id_commande)
        ON DELETE CASCADE,

    CONSTRAINT fk_souscommande_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)

);