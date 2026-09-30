-- ============================================================================
-- Avis et signalements : modération (docs/architecture-acteurs.md §9.10).
--
-- avis : un avis masqué par la modération disparaît de la fiche publique et ne
-- compte plus dans la note de l'espace ; le motif est gardé ici pour son auteur.
-- signalement : on peut désormais signaler un avis (en plus d'un espace ou d'une
-- offre). L'historique complet des décisions est dans journal_action (modules
-- AVIS et SIGNALEMENTS).
--
-- Script rejouable : sert à l'installation complète comme à la mise à jour.
-- ============================================================================

ALTER TABLE avis
    ADD COLUMN IF NOT EXISTS masque BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS motif_moderation TEXT,
    ADD COLUMN IF NOT EXISTS date_moderation TIMESTAMP,
    ADD COLUMN IF NOT EXISTS id_moderateur BIGINT REFERENCES utilisateurs(id_utilisateur) ON DELETE SET NULL;

ALTER TABLE signalement
    ADD COLUMN IF NOT EXISTS id_avis BIGINT REFERENCES avis(id_avis) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_avis_espace_visible ON avis (id_espace) WHERE NOT masque;
CREATE INDEX IF NOT EXISTS idx_signalement_statut ON signalement (statut, date_creation);

COMMENT ON COLUMN avis.masque IS 'Masqué par la modération : absent de la fiche publique et de la note';
COMMENT ON COLUMN signalement.id_avis IS 'Avis signalé (un signalement vise un espace, une offre ou un avis)';
