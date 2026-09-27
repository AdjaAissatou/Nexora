DROP TABLE IF EXISTS reservation CASCADE;

CREATE TABLE reservation (

    id_reservation BIGSERIAL PRIMARY KEY,

    numero VARCHAR(50) UNIQUE NOT NULL,

    id_utilisateur BIGINT NOT NULL,

    id_offre BIGINT NOT NULL,

    id_espace BIGINT NOT NULL,

    id_disponibilite BIGINT,

    id_ressource BIGINT,

    date_reservation TIMESTAMP NOT NULL,

    date_debut TIMESTAMP,

    date_fin TIMESTAMP,

    nombre_personnes INTEGER DEFAULT 1,

    montant NUMERIC(12,2),

    statut statut_reservation DEFAULT 'EN_ATTENTE',

    commentaire_client TEXT,

    commentaire_fournisseur TEXT,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservation_utilisateur
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur),

    CONSTRAINT fk_reservation_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre),
    CONSTRAINT fk_reservation_disponibilite
        FOREIGN KEY(id_disponibilite)
        REFERENCES disponibilite(id_disponibilite),

    CONSTRAINT fk_reservation_ressource
        FOREIGN KEY(id_ressource)
        REFERENCES ressource(id_ressource),
        CONSTRAINT fk_reservation_espace
            FOREIGN KEY(id_espace)
            REFERENCES espace_professionnel(id_espace)

);