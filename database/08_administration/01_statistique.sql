DROP TABLE IF EXISTS statistique CASCADE;

CREATE TABLE statistique (
    id_statistique BIGSERIAL PRIMARY KEY,
    libelle VARCHAR(150) NOT NULL,
    valeur_numerique NUMERIC(18,2),
    valeur_texte TEXT,
    date_statistique DATE NOT NULL DEFAULT CURRENT_DATE,
    module VARCHAR(100),
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);