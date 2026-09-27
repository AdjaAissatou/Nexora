DROP TABLE IF EXISTS commande CASCADE;

CREATE TABLE commande (

    id_commande BIGSERIAL PRIMARY KEY,

    numero VARCHAR(50) UNIQUE NOT NULL,

    id_utilisateur BIGINT NOT NULL,

    id_adresse_utilisateur BIGINT NOT NULL,

    montant_total NUMERIC(12,2) DEFAULT 0,

    frais_livraison NUMERIC(12,2) DEFAULT 0,

    remise NUMERIC(12,2) DEFAULT 0,

    montant_final NUMERIC(12,2) DEFAULT 0,

    statut statut_commande DEFAULT 'EN_ATTENTE',

    commentaire TEXT,

    date_commande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    date_validation TIMESTAMP,

    CONSTRAINT fk_commande_user
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur),

    CONSTRAINT fk_commande_adresse
        FOREIGN KEY(id_adresse_utilisateur)
        REFERENCES adresse_utilisateur(id_adresse_utilisateur)

);