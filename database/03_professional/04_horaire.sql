DROP TABLE IF EXISTS horaire CASCADE;

CREATE TABLE horaire (

    id_horaire BIGSERIAL PRIMARY KEY,

    id_espace BIGINT NOT NULL,

    jour_semaine VARCHAR(20) NOT NULL,

    heure_ouverture TIME,

    heure_fermeture TIME,

    ouvert BOOLEAN DEFAULT TRUE,

    pause_debut TIME,

    pause_fin TIME,

    ouvert_24h BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_horaire_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)

        ON DELETE CASCADE

);