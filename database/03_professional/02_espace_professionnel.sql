DROP TABLE IF EXISTS espace_professionnel CASCADE;

CREATE TABLE espace_professionnel (

    id_espace BIGSERIAL PRIMARY KEY,

    id_utilisateur BIGINT NOT NULL,

    id_type_espace BIGINT NOT NULL,

    nom VARCHAR(200) NOT NULL,

    slogan VARCHAR(255),

    description TEXT,

    telephone VARCHAR(30),

    telephone_secondaire VARCHAR(30),

    email VARCHAR(150),

    site_web VARCHAR(255),

    logo VARCHAR(255),

    couverture VARCHAR(255),

    registre_commerce VARCHAR(100),

    numero_ninea VARCHAR(100),

    numero_rccm VARCHAR(100),

    ouvert BOOLEAN DEFAULT TRUE,

    certifie BOOLEAN DEFAULT FALSE,

    verifie BOOLEAN DEFAULT FALSE,

    note_moyenne NUMERIC(3,2) DEFAULT 0,

    nombre_avis INTEGER DEFAULT 0,

    nombre_vues BIGINT DEFAULT 0,

    nombre_favoris BIGINT DEFAULT 0,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    date_certification TIMESTAMP,
    
    date_verification TIMESTAMP,

    date_modification TIMESTAMP,

    CONSTRAINT fk_espace_utilisateur
        FOREIGN KEY(id_utilisateur)
        REFERENCES utilisateurs(id_utilisateur),

    CONSTRAINT fk_espace_type
        FOREIGN KEY(id_type_espace)
        REFERENCES type_espace(id_type_espace)

);