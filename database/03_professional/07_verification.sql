-- ============================================================================
-- Vérification des espaces professionnels (docs/architecture-acteurs.md §8).
--
-- Une vérification ne fait jamais simplement "verifie = true" : chaque demande,
-- chaque justificatif, chaque contrôle et chaque décision est conservé ici.
-- espace_professionnel.verifie n'est qu'une projection de la dernière demande,
-- écrite par espace-service dans la même transaction que la décision.
--
-- Script rejouable (IF NOT EXISTS) : il sert à l'installation complète comme à
-- la mise à jour d'une base existante. Tables à créer en tant que postgres, puis
-- GRANT à nexora_user (voir 09_seed/21_verification.sql).
-- ============================================================================

DO $$
BEGIN
    CREATE TYPE statut_verification AS ENUM (
        'BROUILLON',     -- dossier en préparation, invisible des agents
        'EN_ATTENTE',    -- soumise, « À traiter »
        'EN_COURS',      -- prise en charge par un agent
        'A_COMPLETER',   -- informations demandées au professionnel
        'APPROUVEE',     -- espace vérifié
        'REFUSEE',       -- refus motivé ; une nouvelle demande reste possible
        'ANNULEE',       -- retirée par le professionnel ou annulée par un admin
        'REVOQUEE'       -- vérification retirée après approbation
    );
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- Référentiel des justificatifs (administrable).
CREATE TABLE IF NOT EXISTS type_justificatif (

    id_type_justificatif BIGSERIAL PRIMARY KEY,

    code VARCHAR(50) NOT NULL UNIQUE,

    libelle VARCHAR(150) NOT NULL,

    description TEXT,

    actif BOOLEAN NOT NULL DEFAULT TRUE

);

-- Règles : justificatifs demandés par type d'espace (NULL = tous les types).
-- Les lignes d'un même groupe_alternatif sont interchangeables : un seul suffit
-- (ex. justificatif d'adresse OU photo de la devanture).
CREATE TABLE IF NOT EXISTS justificatif_requis (

    id_justificatif_requis BIGSERIAL PRIMARY KEY,

    id_type_espace BIGINT REFERENCES type_espace(id_type_espace) ON DELETE CASCADE,

    id_type_justificatif BIGINT NOT NULL REFERENCES type_justificatif(id_type_justificatif) ON DELETE CASCADE,

    obligatoire BOOLEAN NOT NULL DEFAULT TRUE,

    groupe_alternatif VARCHAR(50)

);

CREATE UNIQUE INDEX IF NOT EXISTS ux_justificatif_requis
    ON justificatif_requis (COALESCE(id_type_espace, 0), id_type_justificatif);

-- Une ligne par demande de vérification ; porte l'état courant.
CREATE TABLE IF NOT EXISTS verification_espace (

    id_verification BIGSERIAL PRIMARY KEY,

    id_espace BIGINT NOT NULL REFERENCES espace_professionnel(id_espace) ON DELETE CASCADE,

    id_demandeur BIGINT NOT NULL REFERENCES utilisateurs(id_utilisateur),

    id_agent BIGINT REFERENCES utilisateurs(id_utilisateur) ON DELETE SET NULL,

    statut statut_verification NOT NULL DEFAULT 'BROUILLON',

    -- Dernier motif (infos demandées, refus, annulation, révocation) ;
    -- l'historique complet est dans verification_evenement.
    motif TEXT,

    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    date_soumission TIMESTAMP,

    date_prise_en_charge TIMESTAMP,

    date_decision TIMESTAMP

);

-- Une seule demande ouverte par espace.
CREATE UNIQUE INDEX IF NOT EXISTS ux_verification_ouverte
    ON verification_espace (id_espace)
    WHERE statut IN ('BROUILLON', 'EN_ATTENTE', 'EN_COURS', 'A_COMPLETER');

CREATE INDEX IF NOT EXISTS ix_verification_statut ON verification_espace (statut, date_soumission);
CREATE INDEX IF NOT EXISTS ix_verification_agent ON verification_espace (id_agent);

-- Justificatifs d'une demande. Le fichier est stocké dans un dossier PRIVÉ
-- (jamais sous /uploads, servi publiquement) ; un document remplacé est gardé
-- avec le statut REMPLACE pour que l'historique reste exact.
CREATE TABLE IF NOT EXISTS verification_document (

    id_document BIGSERIAL PRIMARY KEY,

    id_verification BIGINT NOT NULL REFERENCES verification_espace(id_verification) ON DELETE CASCADE,

    id_type_justificatif BIGINT NOT NULL REFERENCES type_justificatif(id_type_justificatif),

    nom_original VARCHAR(255) NOT NULL,

    chemin_stockage VARCHAR(500) NOT NULL,

    type_mime VARCHAR(100) NOT NULL,

    taille BIGINT NOT NULL,

    empreinte_sha256 VARCHAR(64) NOT NULL,

    statut VARCHAR(20) NOT NULL DEFAULT 'DEPOSE'
        CHECK (statut IN ('DEPOSE', 'ACCEPTE', 'REJETE', 'REMPLACE')),

    motif TEXT,

    id_agent BIGINT REFERENCES utilisateurs(id_utilisateur) ON DELETE SET NULL,

    date_depot TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    date_examen TIMESTAMP

);

CREATE INDEX IF NOT EXISTS ix_verification_document ON verification_document (id_verification);

-- Ce que l'agent a réellement contrôlé (≠ complétude automatique).
CREATE TABLE IF NOT EXISTS verification_controle (

    id_controle BIGSERIAL PRIMARY KEY,

    id_verification BIGINT NOT NULL REFERENCES verification_espace(id_verification) ON DELETE CASCADE,

    code VARCHAR(20) NOT NULL
        CHECK (code IN ('IDENTITE', 'ACTIVITE', 'ADRESSE', 'COORDONNEES', 'DOCUMENTS')),

    resultat VARCHAR(20) NOT NULL DEFAULT 'NON_FAIT'
        CHECK (resultat IN ('NON_FAIT', 'CONFORME', 'NON_CONFORME')),

    commentaire TEXT,

    id_agent BIGINT REFERENCES utilisateurs(id_utilisateur) ON DELETE SET NULL,

    date_controle TIMESTAMP,

    CONSTRAINT ux_verification_controle UNIQUE (id_verification, code)

);

-- Historique : jamais modifié ni supprimé par l'application.
CREATE TABLE IF NOT EXISTS verification_evenement (

    id_evenement BIGSERIAL PRIMARY KEY,

    id_verification BIGINT NOT NULL REFERENCES verification_espace(id_verification) ON DELETE CASCADE,

    type VARCHAR(30) NOT NULL,

    ancien_statut statut_verification,

    nouveau_statut statut_verification,

    -- NULL = action du système (ex. révocation automatique).
    id_acteur BIGINT REFERENCES utilisateurs(id_utilisateur) ON DELETE SET NULL,

    role_acteur VARCHAR(20) NOT NULL
        CHECK (role_acteur IN ('PROFESSIONNEL', 'AGENT', 'ADMIN', 'SYSTEME')),

    commentaire TEXT,

    date_evenement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

);

CREATE INDEX IF NOT EXISTS ix_verification_evenement ON verification_evenement (id_verification, date_evenement);
