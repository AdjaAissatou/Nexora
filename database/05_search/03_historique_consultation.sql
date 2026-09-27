DROP TABLE IF EXISTS historique_consultation CASCADE;

CREATE TABLE historique_consultation (

    id_consultation BIGSERIAL PRIMARY KEY,

    id_utilisateur BIGINT,

    id_offre BIGINT,

    id_espace BIGINT,

    date_consultation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    duree_secondes INTEGER,

    CONSTRAINT fk_consultation_user
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur)
        ON DELETE SET NULL,

    CONSTRAINT fk_consultation_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)
        ON DELETE CASCADE,

    CONSTRAINT fk_consultation_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)
        ON DELETE CASCADE

);