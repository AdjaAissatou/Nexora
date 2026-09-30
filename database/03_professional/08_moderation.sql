-- ============================================================================
-- Modération des espaces et des offres (docs/architecture-acteurs.md §9.9).
--
-- Suspendre un espace ou une offre change son statut (SUSPENDU) et garde ici le
-- motif de la dernière décision, pour que le professionnel sache pourquoi et
-- quoi corriger. L'historique complet (qui, quand, pourquoi, depuis quelle
-- adresse) est dans journal_action (module MODERATION).
--
-- Script rejouable : sert à l'installation complète comme à la mise à jour.
-- ============================================================================

ALTER TABLE espace_professionnel
    ADD COLUMN IF NOT EXISTS motif_moderation TEXT,
    ADD COLUMN IF NOT EXISTS date_moderation TIMESTAMP,
    ADD COLUMN IF NOT EXISTS id_moderateur BIGINT REFERENCES utilisateurs(id_utilisateur) ON DELETE SET NULL;

ALTER TABLE offre
    ADD COLUMN IF NOT EXISTS motif_moderation TEXT,
    ADD COLUMN IF NOT EXISTS date_moderation TIMESTAMP,
    ADD COLUMN IF NOT EXISTS id_moderateur BIGINT REFERENCES utilisateurs(id_utilisateur) ON DELETE SET NULL;

COMMENT ON COLUMN espace_professionnel.motif_moderation IS 'Motif de la dernière suspension par la modération (vidé à la réactivation)';
COMMENT ON COLUMN offre.motif_moderation IS 'Motif de la dernière suspension par la modération (vidé à la republication)';
