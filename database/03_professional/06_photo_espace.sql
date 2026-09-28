DROP TABLE IF EXISTS photo_espace CASCADE;

-- Photos du lieu (vitrine, intérieur, etc.) — distinctes du logo et de la
-- couverture, affichées en galerie sur la fiche publique de l'espace.
CREATE TABLE photo_espace (

    id_photo_espace BIGSERIAL PRIMARY KEY,

    id_espace BIGINT NOT NULL,

    url VARCHAR(500) NOT NULL,

    ordre_affichage INTEGER DEFAULT 0,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_photo_espace_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)
        ON DELETE CASCADE

);
