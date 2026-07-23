-- =====================================================================
--  NEXORA - Modele Physique de Donnees (PostgreSQL)
--  Plateforme Marketplace generique (produits & services)
--  Encodage : UTF-8   |   SGBD cible : PostgreSQL 14+
-- =====================================================================
--  Convention : snake_case, cles primaires "id_...", audit sur les
--  entites metier (created_at, updated_at, actif, created_by).
-- =====================================================================

-- ========================= SECURITE / RBAC ===========================

CREATE TABLE profile (
    id_profile   BIGSERIAL PRIMARY KEY,
    libelle      VARCHAR(80)  NOT NULL UNIQUE,
    actif        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE role (
    id_role            SERIAL PRIMARY KEY,
    libelle            VARCHAR(80) NOT NULL UNIQUE,
    date_attribution   TIMESTAMP,
    date_mise_a_jour   TIMESTAMP
);

CREATE TABLE permission (
    id_permission BIGSERIAL PRIMARY KEY,
    libelle       VARCHAR(120) NOT NULL UNIQUE,
    description   VARCHAR(255)
);

CREATE TABLE role_permission (
    id_role       INTEGER NOT NULL REFERENCES role(id_role) ON DELETE CASCADE,
    id_permission BIGINT  NOT NULL REFERENCES permission(id_permission) ON DELETE CASCADE,
    PRIMARY KEY (id_role, id_permission)
);

CREATE TABLE role_profile (
    id_profile BIGINT  NOT NULL REFERENCES profile(id_profile) ON DELETE CASCADE,
    id_role    INTEGER NOT NULL REFERENCES role(id_role) ON DELETE CASCADE,
    PRIMARY KEY (id_profile, id_role)
);

-- ============================ UTILISATEUR =============================

CREATE TABLE utilisateur (
    id_utilisateur     BIGSERIAL PRIMARY KEY,
    nom                VARCHAR(120) NOT NULL,
    prenom             VARCHAR(120),
    email              VARCHAR(180) NOT NULL UNIQUE,
    telephone          VARCHAR(30),
    mot_de_passe       VARCHAR(100) NOT NULL,
    statut_compte      BOOLEAN NOT NULL DEFAULT TRUE,
    genre              VARCHAR(20),
    photo_profil       VARCHAR(500),
    email_verifie      BOOLEAN NOT NULL DEFAULT FALSE,
    telephone_verifie  BOOLEAN NOT NULL DEFAULT FALSE,
    derniere_connexion TIMESTAMP,
    id_profile         BIGINT REFERENCES profile(id_profile),
    -- audit
    created_at         TIMESTAMP,
    updated_at         TIMESTAMP,
    actif              BOOLEAN NOT NULL DEFAULT TRUE,
    created_by         VARCHAR(120)
);
CREATE INDEX idx_utilisateur_telephone ON utilisateur(telephone);

-- ===================== ESPACE PROFESSIONNEL ==========================

CREATE TABLE type_espace_professionnel (
    id_type_espace SERIAL PRIMARY KEY,
    code           VARCHAR(60)  NOT NULL UNIQUE,
    libelle        VARCHAR(120) NOT NULL,
    description    VARCHAR(500),
    created_at     TIMESTAMP, updated_at TIMESTAMP,
    actif          BOOLEAN NOT NULL DEFAULT TRUE, created_by VARCHAR(120)
);

CREATE TABLE categorie_espace (
    id_categorie_espace BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(150) NOT NULL,
    description     VARCHAR(500),
    icone           VARCHAR(100),
    image           VARCHAR(500),
    slug            VARCHAR(180) UNIQUE,
    ordre_affichage INTEGER DEFAULT 0,
    niveau          INTEGER DEFAULT 0,
    actif           BOOLEAN DEFAULT TRUE,
    visible         BOOLEAN DEFAULT TRUE,
    id_parent       BIGINT REFERENCES categorie_espace(id_categorie_espace)
);

CREATE TABLE espace_professionnel (
    id_espace            BIGSERIAL PRIMARY KEY,
    nom_commercial       VARCHAR(200) NOT NULL,
    slug                 VARCHAR(220) UNIQUE,
    description          TEXT,
    logo                 VARCHAR(500),
    banniere             VARCHAR(500),
    telephone_principal  VARCHAR(30),
    telephone_secondaire VARCHAR(30),
    telephone_tertiaire  VARCHAR(30),
    email1               VARCHAR(180),
    email2               VARCHAR(180),
    email3               VARCHAR(180),
    site_web             VARCHAR(255),
    facebook             VARCHAR(255),
    instagram            VARCHAR(255),
    linkedin             VARCHAR(255),
    whatsapp             VARCHAR(30),
    numero_rccm          VARCHAR(80),
    numero_ninea         VARCHAR(80),
    numero_fiscal        VARCHAR(80),
    verifie              BOOLEAN NOT NULL DEFAULT FALSE,
    certifie             BOOLEAN DEFAULT FALSE,
    etat                 BOOLEAN DEFAULT TRUE,
    note_moyenne         DOUBLE PRECISION DEFAULT 0,
    nombre_avis          INTEGER DEFAULT 0,
    nombre_vues          BIGINT DEFAULT 0,
    id_proprietaire      BIGINT REFERENCES utilisateur(id_utilisateur),
    id_type_espace       INTEGER REFERENCES type_espace_professionnel(id_type_espace),
    id_categorie_espace  BIGINT REFERENCES categorie_espace(id_categorie_espace),
    created_at TIMESTAMP, updated_at TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE, created_by VARCHAR(120)
);
CREATE INDEX idx_espace_verifie ON espace_professionnel(verifie);

CREATE TABLE adresse (
    id_adresse BIGSERIAL PRIMARY KEY,
    pays       VARCHAR(100),
    region     VARCHAR(120),
    ville      VARCHAR(120),
    quartier   VARCHAR(120),
    latitude   DOUBLE PRECISION,
    longitude  DOUBLE PRECISION,
    id_espace  BIGINT REFERENCES espace_professionnel(id_espace) ON DELETE CASCADE
);
CREATE INDEX idx_adresse_geo ON adresse(latitude, longitude);

CREATE TABLE horaire (
    id_horaire      BIGSERIAL PRIMARY KEY,
    jour_semaine    VARCHAR(12) NOT NULL,
    heure_ouverture TIME,
    heure_fermeture TIME,
    id_espace       BIGINT REFERENCES espace_professionnel(id_espace) ON DELETE CASCADE
);

CREATE TABLE certification (
    id_certification BIGSERIAL PRIMARY KEY,
    nom              VARCHAR(180) NOT NULL,
    organisme        VARCHAR(180),
    date_obtention   DATE,
    document_url     VARCHAR(500),
    valide           BOOLEAN DEFAULT FALSE,
    id_espace        BIGINT REFERENCES espace_professionnel(id_espace) ON DELETE CASCADE
);

-- ===================== CATALOGUE / OFFRES ============================

CREATE TABLE type_offre (
    id_type_offre BIGSERIAL PRIMARY KEY,
    libelle       VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE type_categorie (
    id_type_categorie SERIAL PRIMARY KEY,
    code        VARCHAR(60) NOT NULL UNIQUE,
    libelle     VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    actif       BOOLEAN DEFAULT TRUE
);

CREATE TABLE categorie_offre (
    id_categorie      BIGSERIAL PRIMARY KEY,
    nom               VARCHAR(150) NOT NULL,
    description       VARCHAR(500),
    icone             VARCHAR(100),
    couleur           VARCHAR(20),
    image             VARCHAR(500),
    slug              VARCHAR(180) UNIQUE,
    ordre_affichage   INTEGER DEFAULT 0,
    niveau            INTEGER DEFAULT 0,
    filtrable         BOOLEAN DEFAULT TRUE,
    visible           BOOLEAN DEFAULT TRUE,
    rechercheable     BOOLEAN DEFAULT TRUE,
    populaire         BOOLEAN DEFAULT FALSE,
    id_parent         BIGINT REFERENCES categorie_offre(id_categorie),
    id_type_categorie INTEGER REFERENCES type_categorie(id_type_categorie),
    created_at TIMESTAMP, updated_at TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE, created_by VARCHAR(120)
);

-- Table unique de la hierarchie Offre / Produit / ServicePro (SINGLE_TABLE)
CREATE TABLE offre (
    id_offre          BIGSERIAL PRIMARY KEY,
    type_offre_dtype  VARCHAR(20) NOT NULL,        -- discriminateur : OFFRE / PRODUIT / SERVICE
    titre             VARCHAR(220) NOT NULL,
    description       TEXT,
    prix              NUMERIC(15,2),
    disponible        BOOLEAN NOT NULL DEFAULT TRUE,
    negociable        BOOLEAN DEFAULT FALSE,
    statut            VARCHAR(30) DEFAULT 'BROUILLON',
    date_creation     TIMESTAMP,
    date_modification TIMESTAMP,
    date_expiration   TIMESTAMP,
    score_pertinence  DOUBLE PRECISION DEFAULT 0,
    nb_consultation   BIGINT DEFAULT 0,
    nb_commandes      BIGINT DEFAULT 0,
    nb_favoris        BIGINT DEFAULT 0,
    id_espace         BIGINT REFERENCES espace_professionnel(id_espace),
    id_categorie      BIGINT REFERENCES categorie_offre(id_categorie),
    id_type_offre     BIGINT REFERENCES type_offre(id_type_offre),
    -- colonnes specifiques Produit
    stock             INTEGER,
    neuf              BOOLEAN,
    marque            VARCHAR(120),
    modele            VARCHAR(120),
    garantie_mois     INTEGER,
    -- colonnes specifiques ServicePro
    type_service        VARCHAR(150),
    tarif               NUMERIC(15,2),
    service_disponible  BOOLEAN,
    a_domicile          BOOLEAN,
    delai_intervention_h INTEGER,
    -- audit
    created_at TIMESTAMP, updated_at TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE, created_by VARCHAR(120)
);
CREATE INDEX idx_offre_statut    ON offre(statut);
CREATE INDEX idx_offre_categorie ON offre(id_categorie);
CREATE INDEX idx_offre_espace    ON offre(id_espace);

CREATE TABLE image (
    id_image   BIGSERIAL PRIMARY KEY,
    url        VARCHAR(500) NOT NULL,
    principale BOOLEAN NOT NULL DEFAULT FALSE,
    alt        VARCHAR(200),
    taille     INTEGER,
    extension  VARCHAR(10),
    ordre      INTEGER DEFAULT 0,
    id_offre   BIGINT REFERENCES offre(id_offre) ON DELETE CASCADE
);

-- ===================== MODELE EAV (attributs dynamiques) =============

CREATE TABLE type_champ (
    id_type_champ BIGSERIAL PRIMARY KEY,
    code          VARCHAR(40)  NOT NULL UNIQUE,   -- TEXT, NUMBER, BOOLEAN, DATE, LIST...
    libelle       VARCHAR(120) NOT NULL,
    composant_ui  VARCHAR(60)
);

CREATE TABLE attribut (
    id_attribut      BIGSERIAL PRIMARY KEY,
    nom              VARCHAR(150) NOT NULL,
    description      VARCHAR(500),
    unite            VARCHAR(40),
    obligatoire      BOOLEAN NOT NULL DEFAULT FALSE,
    filtrable        BOOLEAN NOT NULL DEFAULT FALSE,
    recherche_rapide BOOLEAN DEFAULT FALSE,
    ordre_affichage  INTEGER,
    id_categorie     BIGINT REFERENCES categorie_offre(id_categorie),
    id_type_champ    BIGINT REFERENCES type_champ(id_type_champ),
    created_at TIMESTAMP, updated_at TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE, created_by VARCHAR(120)
);
CREATE INDEX idx_attribut_categorie ON attribut(id_categorie);

CREATE TABLE valeur_attribut_possible (
    id_valeur_attribut BIGSERIAL PRIMARY KEY,
    valeur      VARCHAR(200) NOT NULL,
    ordre       INTEGER DEFAULT 0,
    actif       BOOLEAN DEFAULT TRUE,
    id_attribut BIGINT REFERENCES attribut(id_attribut) ON DELETE CASCADE
);

CREATE TABLE offre_attribut (
    id_offre_attribut  BIGSERIAL PRIMARY KEY,
    valeur_texte       VARCHAR(400),
    valeur_nombre      NUMERIC(18,4),
    valeur_date        DATE,
    valeur_boolean     BOOLEAN,
    valeur_long_texte  TEXT,
    ordre_affichage    INTEGER DEFAULT 0,
    visible            BOOLEAN DEFAULT TRUE,
    id_offre           BIGINT NOT NULL REFERENCES offre(id_offre) ON DELETE CASCADE,
    id_attribut        BIGINT NOT NULL REFERENCES attribut(id_attribut),
    id_valeur_attribut BIGINT REFERENCES valeur_attribut_possible(id_valeur_attribut)
);
CREATE INDEX idx_offreattr_offre     ON offre_attribut(id_offre);
CREATE INDEX idx_offreattr_attribut  ON offre_attribut(id_attribut);
CREATE INDEX idx_offreattr_valtexte  ON offre_attribut(valeur_texte);
CREATE INDEX idx_offreattr_valnombre ON offre_attribut(valeur_nombre);

-- ===================== AVIS / FAVORIS ================================

CREATE TABLE avis (
    id_avis               BIGSERIAL PRIMARY KEY,
    note                  INTEGER NOT NULL CHECK (note BETWEEN 1 AND 5),
    commentaire           TEXT,
    date_avis             TIMESTAMP,
    reponse_professionnel TEXT,
    id_utilisateur        BIGINT NOT NULL REFERENCES utilisateur(id_utilisateur),
    id_offre              BIGINT NOT NULL REFERENCES offre(id_offre) ON DELETE CASCADE,
    created_at TIMESTAMP, updated_at TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE, created_by VARCHAR(120)
);
CREATE INDEX idx_avis_offre ON avis(id_offre);

CREATE TABLE favori (
    id_favori      BIGSERIAL PRIMARY KEY,
    date_favori    TIMESTAMP,
    id_utilisateur BIGINT NOT NULL REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE,
    id_offre       BIGINT NOT NULL REFERENCES offre(id_offre) ON DELETE CASCADE,
    CONSTRAINT uk_favori UNIQUE (id_utilisateur, id_offre)
);

-- ===================== MESSAGERIE / NOTIFICATIONS ====================

CREATE TABLE conversation (
    id_conversation       BIGSERIAL PRIMARY KEY,
    date_creation         TIMESTAMP,
    actif                 BOOLEAN NOT NULL DEFAULT TRUE,
    dernier_message       VARCHAR(500),
    date_dernier_message  TIMESTAMP,
    archive_client        BOOLEAN DEFAULT FALSE,
    archive_professionnel BOOLEAN DEFAULT FALSE,
    id_client             BIGINT REFERENCES utilisateur(id_utilisateur),
    id_espace             BIGINT REFERENCES espace_professionnel(id_espace)
);

CREATE TABLE message (
    id_message            BIGSERIAL PRIMARY KEY,
    contenu               TEXT,
    date_envoi            TIMESTAMP,
    lu                    BOOLEAN DEFAULT FALSE,
    type_message          VARCHAR(30),
    piece_jointe          VARCHAR(500),
    supprime_expediteur   BOOLEAN DEFAULT FALSE,
    supprime_destinataire BOOLEAN DEFAULT FALSE,
    modifie               BOOLEAN DEFAULT FALSE,
    date_modification     TIMESTAMP,
    id_conversation       BIGINT NOT NULL REFERENCES conversation(id_conversation) ON DELETE CASCADE,
    id_expediteur         BIGINT NOT NULL REFERENCES utilisateur(id_utilisateur)
);
CREATE INDEX idx_message_conversation ON message(id_conversation);

CREATE TABLE appel (
    id_appel        BIGSERIAL PRIMARY KEY,
    date_debut      TIMESTAMP,
    date_fin        TIMESTAMP,
    statut          VARCHAR(30),
    type_appel      VARCHAR(10),
    id_conversation BIGINT NOT NULL REFERENCES conversation(id_conversation) ON DELETE CASCADE
);

CREATE TABLE type_notification (
    id_type_notification BIGSERIAL PRIMARY KEY,
    libelle              VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE notification (
    id_notification      BIGSERIAL PRIMARY KEY,
    contenu              VARCHAR(500) NOT NULL,
    lu                   BOOLEAN DEFAULT FALSE,
    date_lecture         TIMESTAMP,
    lien                 VARCHAR(500),
    importance           VARCHAR(20),
    date_creation        TIMESTAMP,
    id_utilisateur       BIGINT NOT NULL REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE,
    id_type_notification BIGINT REFERENCES type_notification(id_type_notification)
);
CREATE INDEX idx_notif_user_lu ON notification(id_utilisateur, lu);

-- ===================== COMMANDE / PANIER =============================

CREATE TABLE mode_paiement (
    id_mode_paiement SERIAL PRIMARY KEY,
    libelle          VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE statut_paiement (
    id_statut_paiement SERIAL PRIMARY KEY,
    libelle            VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE commande (
    id_commande         BIGSERIAL PRIMARY KEY,
    numero_commande     VARCHAR(40) NOT NULL UNIQUE,
    date_commande       TIMESTAMP,
    statut_commande     VARCHAR(30) DEFAULT 'EN_ATTENTE',
    total               NUMERIC(15,2) DEFAULT 0,
    adresse_livraison   VARCHAR(400),
    date_prestation     DATE,
    heure_prestation    TIME,
    statut_prestation   VARCHAR(30),
    confirmation_client BOOLEAN DEFAULT FALSE,
    id_client           BIGINT NOT NULL REFERENCES utilisateur(id_utilisateur),
    id_espace           BIGINT NOT NULL REFERENCES espace_professionnel(id_espace),
    id_mode_paiement    INTEGER REFERENCES mode_paiement(id_mode_paiement),
    created_at TIMESTAMP, updated_at TIMESTAMP,
    actif BOOLEAN NOT NULL DEFAULT TRUE, created_by VARCHAR(120)
);
CREATE INDEX idx_commande_client ON commande(id_client);

CREATE TABLE ligne_commande (
    id_ligne_commande BIGSERIAL PRIMARY KEY,
    quantite      NUMERIC(12,2) NOT NULL DEFAULT 1,
    prix_unitaire NUMERIC(15,2),
    sous_total    NUMERIC(15,2),
    id_commande   BIGINT NOT NULL REFERENCES commande(id_commande) ON DELETE CASCADE,
    id_offre      BIGINT NOT NULL REFERENCES offre(id_offre)
);

CREATE TABLE historique_statut_commande (
    id_historique   BIGSERIAL PRIMARY KEY,
    statut          VARCHAR(30),
    date_changement TIMESTAMP,
    commentaire     VARCHAR(300),
    id_commande     BIGINT NOT NULL REFERENCES commande(id_commande) ON DELETE CASCADE
);

CREATE TABLE livraison (
    id_livraison     BIGSERIAL PRIMARY KEY,
    transporteur     VARCHAR(150),
    numero_suivi     VARCHAR(80),
    statut_livraison VARCHAR(30),
    date_expedition  TIMESTAMP,
    date_livraison   TIMESTAMP,
    frais_livraison  NUMERIC(12,2),
    distance         DOUBLE PRECISION,
    heure_estimee    TIMESTAMP,
    id_commande      BIGINT NOT NULL UNIQUE REFERENCES commande(id_commande) ON DELETE CASCADE
);

CREATE TABLE panier (
    id_panier      BIGSERIAL PRIMARY KEY,
    date_creation  TIMESTAMP,
    id_utilisateur BIGINT NOT NULL UNIQUE REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE
);

CREATE TABLE ligne_panier (
    id_ligne_panier BIGSERIAL PRIMARY KEY,
    quantite   NUMERIC(12,2) NOT NULL DEFAULT 1,
    id_panier  BIGINT NOT NULL REFERENCES panier(id_panier) ON DELETE CASCADE,
    id_offre   BIGINT NOT NULL REFERENCES offre(id_offre),
    CONSTRAINT uk_ligne_panier UNIQUE (id_panier, id_offre)
);

-- ===================== PAIEMENT / WALLET / ESCROW ====================

CREATE TABLE paiement (
    id_paiement           BIGSERIAL PRIMARY KEY,
    montant               NUMERIC(15,2),
    date_paiement         TIMESTAMP,
    reference_transaction VARCHAR(120),
    methode_paiement      VARCHAR(80),
    devise                VARCHAR(8) DEFAULT 'XOF',
    id_commande           BIGINT NOT NULL UNIQUE REFERENCES commande(id_commande) ON DELETE CASCADE,
    id_statut_paiement    INTEGER REFERENCES statut_paiement(id_statut_paiement)
);

CREATE TABLE wallet (
    id_wallet        BIGSERIAL PRIMARY KEY,
    solde_disponible NUMERIC(15,2) DEFAULT 0,
    solde_bloque     NUMERIC(15,2) DEFAULT 0,
    id_utilisateur   BIGINT NOT NULL UNIQUE REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE
);

CREATE TABLE transaction (
    id_transaction        BIGSERIAL PRIMARY KEY,
    montant_total         NUMERIC(15,2),
    montant_vendeur       NUMERIC(15,2),
    commission_plateforme NUMERIC(15,2),
    type_transaction      VARCHAR(40),
    date_transaction      TIMESTAMP,
    id_wallet             BIGINT NOT NULL REFERENCES wallet(id_wallet) ON DELETE CASCADE
);

CREATE TABLE escrow (
    id_escrow       BIGSERIAL PRIMARY KEY,
    montant_blocage NUMERIC(15,2),
    date_blocage    TIMESTAMP,
    date_liberation TIMESTAMP,
    statut          VARCHAR(30),
    id_commande     BIGINT NOT NULL UNIQUE REFERENCES commande(id_commande) ON DELETE CASCADE
);

CREATE TABLE promotion (
    id_promotion   BIGSERIAL PRIMARY KEY,
    code_promo     VARCHAR(60) UNIQUE,
    valeur         NUMERIC(12,2),
    type_reduction VARCHAR(30),
    date_debut     DATE,
    date_fin       DATE,
    actif          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE litige (
    id_litige      BIGSERIAL PRIMARY KEY,
    motif          VARCHAR(200),
    description    TEXT,
    statut         VARCHAR(30),
    decision_admin TEXT,
    date_ouverture TIMESTAMP,
    id_commande    BIGINT NOT NULL UNIQUE REFERENCES commande(id_commande) ON DELETE CASCADE
);

-- =====================================================================
--  Fin du schema. Voir seed.sql pour les donnees de configuration.
-- =====================================================================
