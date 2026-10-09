-- ============================================================================
-- 09/10 : sécurité du compte (docs/architecture-acteurs.md §25).
--
-- connexion_compte : chaque tentative de connexion d'un compte existant. Réussie, elle ouvre une
--   session (id_session, inscrit dans les jetons sous « sid ») : dernière activité mise à jour à chaque
--   rafraîchissement, fin à la déconnexion ou quand on la révoque depuis « Sécurité » (le jeton d'accès
--   en cours expire au plus tard 15 minutes après). Échouée (mauvais mot de passe) : gardée pour l'alerte.
-- changement_email : nouvelle adresse en attente, confirmée par un code envoyé à cette adresse.
-- Rejouable.
-- ============================================================================

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

CREATE TABLE IF NOT EXISTS connexion_compte (
    id_connexion BIGSERIAL PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    id_session UUID UNIQUE,
    reussie BOOLEAN NOT NULL,
    date_connexion TIMESTAMP NOT NULL DEFAULT NOW(),
    derniere_activite TIMESTAMP NOT NULL DEFAULT NOW(),
    adresse_ip VARCHAR(50),
    user_agent VARCHAR(500),
    date_fin TIMESTAMP,
    motif_fin VARCHAR(30),
    CONSTRAINT ck_connexion_session CHECK (reussie = (id_session IS NOT NULL)),
    CONSTRAINT ck_connexion_motif CHECK (motif_fin IS NULL OR motif_fin IN
        ('DECONNEXION', 'REVOQUEE', 'MOT_DE_PASSE', 'EMAIL', 'AUTRES_APPAREILS'))
);
CREATE INDEX IF NOT EXISTS idx_connexion_compte_compte ON connexion_compte (account_id, date_connexion DESC);

CREATE TABLE IF NOT EXISTS changement_email (
    id_changement BIGSERIAL PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    nouvel_email VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL,
    date_demande TIMESTAMP NOT NULL DEFAULT NOW(),
    date_expiration TIMESTAMP NOT NULL,
    date_confirmation TIMESTAMP,
    essais INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_changement_email_compte ON changement_email (account_id, date_demande DESC);
