-- ============================================================================
-- Nexora Découvrir (docs/architecture-acteurs.md §18) : ce que chaque visiteur fait dans le
-- flux, pour apprendre ses goûts. Un signal = une interaction avec une offre ou un espace.
--
-- visiteur : identifiant anonyme du navigateur (cookie), ou « c-<compte> » une fois connecté
--            (le profil suit alors la personne d'un appareil à l'autre).
-- Poids (DecouverteService) : PASSE −0,6 · VUE 0,2 · VUE_LONGUE 1 · J_AIME 3 · PARTAGE 3 ·
-- ENREGISTRE 4 · DETAIL 4 · TAILLES 5 · CONTACT 7 · ACHAT 8 ; atténués avec le temps.
-- Créée si absente : jamais vidée par une mise à jour.
-- ============================================================================

SET client_encoding = 'UTF8';

CREATE TABLE IF NOT EXISTS decouverte_signal (
    id_signal BIGSERIAL PRIMARY KEY,
    visiteur VARCHAR(64) NOT NULL,
    id_offre BIGINT REFERENCES offre(id_offre) ON DELETE CASCADE,
    id_espace BIGINT REFERENCES espace_professionnel(id_espace) ON DELETE CASCADE,
    type_signal VARCHAR(20) NOT NULL,
    duree_ms INTEGER,
    date_signal TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_decouverte_signal_type CHECK (type_signal IN
        ('VUE', 'VUE_LONGUE', 'PASSE', 'J_AIME', 'ENREGISTRE', 'PARTAGE', 'DETAIL', 'TAILLES', 'CONTACT', 'ACHAT')),
    CONSTRAINT ck_decouverte_signal_cible CHECK (id_offre IS NOT NULL OR id_espace IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_decouverte_signal_visiteur ON decouverte_signal (visiteur, date_signal DESC);
CREATE INDEX IF NOT EXISTS idx_decouverte_signal_offre ON decouverte_signal (id_offre);
