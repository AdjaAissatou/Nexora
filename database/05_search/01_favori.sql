DROP TABLE IF EXISTS favori CASCADE;

CREATE TABLE favori (

    id_favori BIGSERIAL PRIMARY KEY,

    id_utilisateur BIGINT NOT NULL,

    id_offre BIGINT,

    id_espace BIGINT,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_favori_user
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur),

    CONSTRAINT fk_favori_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)
        ON DELETE CASCADE,

    CONSTRAINT fk_favori_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)
        ON DELETE CASCADE

);