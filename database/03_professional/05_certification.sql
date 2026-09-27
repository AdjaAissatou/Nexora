DROP TABLE IF EXISTS certification CASCADE;

CREATE TABLE certification (

    id_certification BIGSERIAL PRIMARY KEY,

    id_espace BIGINT NOT NULL,

    organisme VARCHAR(200),

    nom VARCHAR(200),

    numero VARCHAR(100),

    document VARCHAR(255),

    date_obtention DATE,

    date_expiration DATE,

    statut statut_certification DEFAULT 'EN_ATTENTE',

    commentaire TEXT,

    verifie_par BIGINT,

    date_verification TIMESTAMP,

    CONSTRAINT fk_certification_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)

        ON DELETE CASCADE,

    CONSTRAINT fk_certification_admin
        FOREIGN KEY(verifie_par)
        REFERENCES utilisateurs(id_utilisateur)

);