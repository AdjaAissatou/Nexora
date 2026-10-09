-- ============================================================================
-- 09/10 : popularité des offres (docs/architecture-acteurs.md §23).
--
-- Compteurs tenus par la base, quel que soit le service qui agit :
--   offre.vues_decouvrir : cartes regardées au moins une seconde dans Découvrir (VUE, VUE_LONGUE) ;
--                          s'ajoutent à offre.vue_count (fiche ouverte). Un compteur : il ne
--                          redescend pas quand un visiteur efface ses goûts ;
--   offre.nombre_jaime   : visiteurs distincts qui aiment l'offre dans Découvrir (retiré si on n'aime plus) ;
--   offre.nombre_favoris : comptes qui l'ont en favori (cœur de la fiche ou « Enregistrer » de Découvrir) ;
--   espace_professionnel.nombre_jaime : j'aime sur la fiche de l'espace dans Découvrir.
-- offre.score_popularite (calculé) : vues + ½ vue Découvrir + 3 j'aime + 5 favoris ; sert au tri
-- « Popularité », au badge « 🔥 Populaire » et au flux Découvrir. Rejouable.
-- ============================================================================

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

ALTER TABLE offre ADD COLUMN IF NOT EXISTS vues_decouvrir INTEGER NOT NULL DEFAULT 0;
ALTER TABLE offre ADD COLUMN IF NOT EXISTS nombre_jaime INTEGER NOT NULL DEFAULT 0;
ALTER TABLE offre ADD COLUMN IF NOT EXISTS nombre_favoris INTEGER NOT NULL DEFAULT 0;
ALTER TABLE espace_professionnel ADD COLUMN IF NOT EXISTS nombre_jaime INTEGER NOT NULL DEFAULT 0;
ALTER TABLE offre ADD COLUMN IF NOT EXISTS score_popularite NUMERIC(12, 1) GENERATED ALWAYS AS
    (COALESCE(vue_count, 0) + 0.5 * vues_decouvrir + 3 * nombre_jaime + 5 * nombre_favoris) STORED;
CREATE INDEX IF NOT EXISTS idx_offre_score_popularite ON offre (score_popularite DESC);
CREATE INDEX IF NOT EXISTS idx_decouverte_signal_jaime ON decouverte_signal (id_offre, id_espace) WHERE type_signal = 'J_AIME';

COMMENT ON COLUMN offre.vues_decouvrir IS 'Cartes regardées au moins une seconde dans Découvrir (compteur)';
COMMENT ON COLUMN offre.nombre_jaime IS 'Visiteurs distincts qui aiment l''offre dans Découvrir';
COMMENT ON COLUMN offre.nombre_favoris IS 'Comptes qui ont l''offre en favori';
COMMENT ON COLUMN offre.score_popularite IS 'vues + ½ vue Découvrir + 3 j''aime + 5 favoris';

CREATE OR REPLACE FUNCTION recompter_jaime(offre_ BIGINT, espace_ BIGINT) RETURNS VOID AS $$
BEGIN
    IF offre_ IS NOT NULL THEN
        UPDATE offre SET nombre_jaime = (SELECT COUNT(DISTINCT visiteur) FROM decouverte_signal
                                         WHERE type_signal = 'J_AIME' AND id_offre = offre_)
        WHERE id_offre = offre_;
    ELSIF espace_ IS NOT NULL THEN
        UPDATE espace_professionnel SET nombre_jaime = (SELECT COUNT(DISTINCT visiteur) FROM decouverte_signal
                                                        WHERE type_signal = 'J_AIME' AND id_offre IS NULL AND id_espace = espace_)
        WHERE id_espace = espace_;
    END IF;
END $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION decouverte_signal_compter() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.type_signal IN ('VUE', 'VUE_LONGUE') AND NEW.id_offre IS NOT NULL THEN
            UPDATE offre SET vues_decouvrir = vues_decouvrir + 1 WHERE id_offre = NEW.id_offre;
        ELSIF NEW.type_signal = 'J_AIME' THEN
            PERFORM recompter_jaime(NEW.id_offre, NEW.id_espace);
        END IF;
    ELSIF TG_OP = 'DELETE' THEN
        IF OLD.type_signal = 'J_AIME' THEN PERFORM recompter_jaime(OLD.id_offre, OLD.id_espace); END IF;
    ELSE -- UPDATE : deux profils du même visiteur fusionnés à la connexion
        IF NEW.type_signal = 'J_AIME' THEN PERFORM recompter_jaime(NEW.id_offre, NEW.id_espace); END IF;
    END IF;
    RETURN NULL;
END $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_decouverte_signal_compter ON decouverte_signal;
CREATE TRIGGER trg_decouverte_signal_compter AFTER INSERT OR DELETE OR UPDATE OF visiteur ON decouverte_signal
    FOR EACH ROW EXECUTE FUNCTION decouverte_signal_compter();

-- Favoris d'une offre (le déclencheur trg_favori_compter, lui, tient le total de l'espace)
CREATE OR REPLACE FUNCTION favori_compter_offre() RETURNS TRIGGER AS $$
DECLARE offre_ BIGINT := CASE WHEN TG_OP = 'DELETE' THEN OLD.id_offre ELSE NEW.id_offre END;
BEGIN
    IF offre_ IS NOT NULL THEN
        UPDATE offre SET nombre_favoris = (SELECT COUNT(*) FROM favori f WHERE f.id_offre = offre_) WHERE id_offre = offre_;
    END IF;
    RETURN NULL;
END $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_favori_compter_offre ON favori;
CREATE TRIGGER trg_favori_compter_offre AFTER INSERT OR DELETE ON favori FOR EACH ROW EXECUTE FUNCTION favori_compter_offre();

-- Comptes justes dès maintenant (rejouable : recalcul complet, sauf les vues Découvrir déjà comptées)
UPDATE offre o SET nombre_favoris = (SELECT COUNT(*) FROM favori f WHERE f.id_offre = o.id_offre);
UPDATE offre o SET nombre_jaime = (SELECT COUNT(DISTINCT visiteur) FROM decouverte_signal s
                                   WHERE s.type_signal = 'J_AIME' AND s.id_offre = o.id_offre);
UPDATE espace_professionnel e SET nombre_jaime = (SELECT COUNT(DISTINCT visiteur) FROM decouverte_signal s
                                                  WHERE s.type_signal = 'J_AIME' AND s.id_offre IS NULL AND s.id_espace = e.id_espace);
UPDATE offre o SET vues_decouvrir = (SELECT COUNT(*) FROM decouverte_signal s
                                     WHERE s.type_signal IN ('VUE', 'VUE_LONGUE') AND s.id_offre = o.id_offre)
WHERE o.vues_decouvrir = 0;
