DROP TABLE IF EXISTS horaire_exception CASCADE;

CREATE TABLE horaire_exception (

    id_exception BIGSERIAL PRIMARY KEY,

    id_espace BIGINT NOT NULL,

    date_exception DATE NOT NULL,

    heure_ouverture TIME,

    heure_fermeture TIME,

    ferme BOOLEAN DEFAULT FALSE,

    motif VARCHAR(255),

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_exception_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)
        ON DELETE CASCADE

);