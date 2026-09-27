CREATE TABLE utilisateurs (
    id_utilisateur BIGSERIAL PRIMARY KEY,

    uuid UUID NOT NULL DEFAULT uuid_generate_v4(),

    account_id UUID UNIQUE,

    nom VARCHAR(100) NOT NULL,

    prenom VARCHAR(100) NOT NULL,

    pseudo VARCHAR(80) UNIQUE,

    photo_profil TEXT,
    photo_couverture TEXT,
    biographie TEXT,

    date_naissance DATE,

    sexe sexe,

    email VARCHAR(255) UNIQUE NOT NULL,

    telephone VARCHAR(30) UNIQUE,

    email_verifie BOOLEAN DEFAULT FALSE,

    telephone_verifie BOOLEAN DEFAULT FALSE,

    langue langue DEFAULT 'FRANCAIS',

    double_auth BOOLEAN DEFAULT FALSE,

    tentative_connexion INTEGER DEFAULT 0,

    compte_bloque BOOLEAN DEFAULT FALSE,

    derniere_connexion TIMESTAMP,

    statut statut_compte DEFAULT 'EN_ATTENTE',

    actif BOOLEAN DEFAULT TRUE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    date_modification TIMESTAMP,

    CONSTRAINT fk_utilisateur_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
        ON DELETE CASCADE
);