DROP TABLE IF EXISTS historique_recherche CASCADE;

CREATE TABLE historique_recherche (

    id_historique BIGSERIAL PRIMARY KEY,

    id_utilisateur BIGINT,

    mot_cle VARCHAR(255),

    categorie VARCHAR(100),

    latitude NUMERIC(10,7),

    longitude NUMERIC(10,7),

    rayon_km INTEGER,

    nombre_resultats INTEGER DEFAULT 0,

    date_recherche TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_hist_user
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur)
        ON DELETE SET NULL

);