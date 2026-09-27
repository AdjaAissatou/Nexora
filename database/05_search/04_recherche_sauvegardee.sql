DROP TABLE IF EXISTS recherche_sauvegardee CASCADE;

CREATE TABLE recherche_sauvegardee (

    id_recherche BIGSERIAL PRIMARY KEY,

    id_utilisateur BIGINT NOT NULL,

    nom VARCHAR(150),

    mot_cle VARCHAR(255),

    categorie VARCHAR(100),

    rayon_km INTEGER,

    latitude NUMERIC(10,7),

    longitude NUMERIC(10,7),

    active BOOLEAN DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recherche_user
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur)
        ON DELETE CASCADE

);