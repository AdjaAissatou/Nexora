DROP TABLE IF EXISTS moyen_contact CASCADE;

CREATE TABLE moyen_contact (

    id_moyen_contact BIGSERIAL PRIMARY KEY,

    id_espace BIGINT NOT NULL,

    type type_contact NOT NULL,

    libelle VARCHAR(100),

    valeur VARCHAR(255) NOT NULL,

    principal BOOLEAN DEFAULT FALSE,

    visible BOOLEAN DEFAULT TRUE,

    ordre_affichage INTEGER DEFAULT 0,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_contact_espace
        FOREIGN KEY(id_espace)
        REFERENCES espace_professionnel(id_espace)
        ON DELETE CASCADE

);