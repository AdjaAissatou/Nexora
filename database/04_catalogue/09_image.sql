DROP TABLE IF EXISTS image CASCADE;

CREATE TABLE image (

    id_image BIGSERIAL PRIMARY KEY,

    id_offre BIGINT NOT NULL,

    url VARCHAR(500) NOT NULL,

    principale BOOLEAN DEFAULT FALSE,

    ordre_affichage INTEGER DEFAULT 0,

    texte_alternatif VARCHAR(255),

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_image_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)
        ON DELETE CASCADE

);