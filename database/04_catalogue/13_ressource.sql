DROP TABLE IF EXISTS ressource CASCADE;

CREATE TABLE ressource (

    id_ressource BIGSERIAL PRIMARY KEY,

    id_offre BIGINT NOT NULL,

    nom VARCHAR(150) NOT NULL,

    code VARCHAR(50),

    description TEXT,

    type VARCHAR(100),

    capacite INTEGER DEFAULT 1,

    disponible BOOLEAN DEFAULT TRUE,

    actif BOOLEAN DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ressource_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)
        ON DELETE CASCADE

);