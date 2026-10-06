-- ============================================================================
-- 06/10 : compteurs d'un espace (vues, favoris, avis) qui ne bougeaient pas.
--
-- 1. Un espace créé depuis l'application avait ses compteurs à NULL ; « NULL + 1 » restant
--    NULL, ses vues ne montaient jamais. Compteurs à 0 et valeur par défaut 0.
-- 2. Le nombre de favoris d'un espace n'était tenu à jour par aucun service : un déclencheur
--    le recalcule à chaque favori ajouté ou retiré (favoris de l'espace et de ses offres).
-- Rejouable.
-- ============================================================================

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

UPDATE espace_professionnel SET nombre_vues = 0 WHERE nombre_vues IS NULL;
UPDATE espace_professionnel SET nombre_favoris = 0 WHERE nombre_favoris IS NULL;
UPDATE espace_professionnel SET nombre_avis = 0 WHERE nombre_avis IS NULL;
UPDATE espace_professionnel SET note_moyenne = 0 WHERE note_moyenne IS NULL;
UPDATE offre SET vue_count = 0 WHERE vue_count IS NULL;
ALTER TABLE espace_professionnel ALTER COLUMN nombre_vues SET DEFAULT 0;
ALTER TABLE espace_professionnel ALTER COLUMN nombre_favoris SET DEFAULT 0;
ALTER TABLE espace_professionnel ALTER COLUMN nombre_avis SET DEFAULT 0;
ALTER TABLE offre ALTER COLUMN vue_count SET DEFAULT 0;

CREATE OR REPLACE FUNCTION recompter_favoris_espace(id_ BIGINT) RETURNS VOID AS $$
    UPDATE espace_professionnel ep SET nombre_favoris = (
        SELECT COUNT(*) FROM favori f
        WHERE f.id_espace = id_ OR f.id_offre IN (SELECT o.id_offre FROM offre o WHERE o.id_espace = id_))
    WHERE ep.id_espace = id_;
$$ LANGUAGE sql;

CREATE OR REPLACE FUNCTION favori_compter() RETURNS TRIGGER AS $$
DECLARE ligne favori%ROWTYPE;
BEGIN
    ligne := CASE WHEN TG_OP = 'DELETE' THEN OLD ELSE NEW END;
    IF ligne.id_espace IS NOT NULL THEN PERFORM recompter_favoris_espace(ligne.id_espace); END IF;
    IF ligne.id_offre IS NOT NULL THEN
        PERFORM recompter_favoris_espace(o.id_espace) FROM offre o WHERE o.id_offre = ligne.id_offre;
    END IF;
    RETURN NULL;
END $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_favori_compter ON favori;
CREATE TRIGGER trg_favori_compter AFTER INSERT OR DELETE ON favori FOR EACH ROW EXECUTE FUNCTION favori_compter();

-- Comptes justes dès maintenant
DO $$ BEGIN PERFORM recompter_favoris_espace(id_espace) FROM espace_professionnel; END $$;
