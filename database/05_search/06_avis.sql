DROP TABLE IF EXISTS avis CASCADE;

CREATE TABLE avis (

    id_avis BIGSERIAL PRIMARY KEY,

    id_utilisateur BIGINT NOT NULL,

    id_espace BIGINT,

    id_offre BIGINT,

    note INTEGER NOT NULL,

    commentaire TEXT,

    reponse_fournisseur TEXT,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    date_reponse TIMESTAMP,

    CONSTRAINT chk_note
        CHECK(note BETWEEN 1 AND 5),

    CONSTRAINT fk_avis_user
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur),

    CONSTRAINT fk_avis_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)
        ON DELETE CASCADE,

    CONSTRAINT fk_avis_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)
        ON DELETE CASCADE

);