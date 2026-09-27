DROP TABLE IF EXISTS media CASCADE;

CREATE TABLE media (

    id_media BIGSERIAL PRIMARY KEY,

    nom_fichier VARCHAR(255) NOT NULL,

    nom_original VARCHAR(255),

    chemin VARCHAR(500) NOT NULL,

    type_mime VARCHAR(100),

    extension VARCHAR(20),

    taille BIGINT,

    type_media type_media NOT NULL,

    largeur INTEGER,

    hauteur INTEGER,

    duree INTEGER,

    actif BOOLEAN DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);