DROP TABLE IF EXISTS livraison CASCADE;

CREATE TABLE livraison (

    id_livraison BIGSERIAL PRIMARY KEY,

    id_sous_commande BIGINT NOT NULL,

    transporteur VARCHAR(150),

    numero_suivi VARCHAR(100),

    mode_livraison VARCHAR(100),

    date_expedition TIMESTAMP,

    date_livraison_prevue TIMESTAMP,

    date_livraison_effective TIMESTAMP,

    statut statut_livraison DEFAULT 'EN_PREPARATION',

    commentaire TEXT,

    CONSTRAINT fk_livraison_sous_commande
        FOREIGN KEY(id_sous_commande)
        REFERENCES sous_commande(id_sous_commande)
        ON DELETE CASCADE

);