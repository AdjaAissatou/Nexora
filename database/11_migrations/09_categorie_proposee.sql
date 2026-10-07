-- ============================================================================
-- 07/10 : « Autre… » à l'ajout d'une offre (docs/architecture-acteurs.md §21).
--
-- Quand aucune catégorie du rayon ne convient, le professionnel écrit la sienne. L'offre est
-- rangée dans le rayon principal du type de son espace (type « Autre ») et garde le texte saisi :
-- il est cherchable et remonte à l'administration, qui crée la catégorie, rattache les offres
-- à une catégorie existante ou écarte la proposition. Rejouable.
-- ============================================================================

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

ALTER TABLE offre ADD COLUMN IF NOT EXISTS categorie_proposee VARCHAR(120);
COMMENT ON COLUMN offre.categorie_proposee IS
    'Catégorie écrite par le professionnel (« Autre… ») en attente de décision de l''administration';
CREATE INDEX IF NOT EXISTS idx_offre_categorie_proposee ON offre (id_offre) WHERE categorie_proposee IS NOT NULL;
