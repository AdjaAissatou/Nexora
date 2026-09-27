DROP TABLE IF EXISTS parametre CASCADE;

CREATE TABLE parametre (
    id_parametre BIGSERIAL PRIMARY KEY,

    code VARCHAR(100) NOT NULL UNIQUE,
    libelle VARCHAR(150) NOT NULL,
    description TEXT,

    valeur_texte TEXT,
    valeur_numerique NUMERIC(18,2),
    valeur_booleenne BOOLEAN,
    valeur_date DATE,

    categorie VARCHAR(100),
    module VARCHAR(100),

    actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP
);