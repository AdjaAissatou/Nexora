-- ============================================================================
-- 04/10 : pastilles de couleur et valeurs épuisées (tailles, couleurs…) d'une offre.
-- Rejouable.
-- ============================================================================

SET client_encoding = 'UTF8';

ALTER TABLE valeur_attribut_possible ADD COLUMN IF NOT EXISTS code_couleur VARCHAR(7);
ALTER TABLE offre_attribut ADD COLUMN IF NOT EXISTS epuise BOOLEAN NOT NULL DEFAULT FALSE;
