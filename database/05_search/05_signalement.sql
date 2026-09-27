DROP TABLE IF EXISTS signalement CASCADE;

CREATE TABLE signalement (

    id_signalement BIGSERIAL PRIMARY KEY,

    id_utilisateur BIGINT NOT NULL,

    id_offre BIGINT,

    id_espace BIGINT,

    motif VARCHAR(255),

    description TEXT,

    statut statut_signalement DEFAULT 'EN_ATTENTE',

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    traite_par BIGINT,

    date_traitement TIMESTAMP,

    commentaire_admin TEXT,

    CONSTRAINT fk_signalement_user
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur),

    CONSTRAINT fk_signalement_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)
        ON DELETE CASCADE,

    CONSTRAINT fk_signalement_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)
        ON DELETE CASCADE,

    CONSTRAINT fk_signalement_admin
        FOREIGN KEY(traite_par)
        REFERENCES utilisateurs(id_utilisateur)

);