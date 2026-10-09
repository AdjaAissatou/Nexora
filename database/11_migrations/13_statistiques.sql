-- ============================================================================
-- 09/10 : statistiques détaillées du professionnel (docs/architecture-acteurs.md §26).
--
-- evenement_statistique : ce qui n'était que compté devient daté, pour suivre l'évolution semaine par
-- semaine. Vue d'une fiche d'offre ou d'espace (déclencheurs sur offre.vue_count et
-- espace_professionnel.nombre_vues, quel que soit le service qui compte) et clics sur Appeler,
-- WhatsApp, Itinéraire et Partager (relayés par le web). Les vues Découvrir, les j'aime et les
-- favoris sont déjà datés (decouverte_signal, favori). Table en ajout seul : rien n'y est modifié.
-- Rejouable.
-- ============================================================================

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

CREATE TABLE IF NOT EXISTS evenement_statistique (
    id_evenement BIGSERIAL PRIMARY KEY,
    id_espace BIGINT NOT NULL REFERENCES espace_professionnel(id_espace) ON DELETE CASCADE,
    id_offre BIGINT REFERENCES offre(id_offre) ON DELETE CASCADE,
    type_evenement VARCHAR(20) NOT NULL,
    visiteur VARCHAR(64),
    date_evenement TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_evenement_statistique_type CHECK (type_evenement IN
        ('VUE_OFFRE', 'VUE_ESPACE', 'APPEL', 'WHATSAPP', 'ITINERAIRE', 'PARTAGE'))
);
CREATE INDEX IF NOT EXISTS idx_evenement_statistique_espace ON evenement_statistique (id_espace, date_evenement);
CREATE INDEX IF NOT EXISTS idx_evenement_statistique_offre ON evenement_statistique (id_offre, date_evenement);
CREATE INDEX IF NOT EXISTS idx_decouverte_signal_date ON decouverte_signal (date_signal);

CREATE OR REPLACE FUNCTION evenement_vue_offre() RETURNS TRIGGER AS $$
BEGIN
    IF COALESCE(NEW.vue_count, 0) > COALESCE(OLD.vue_count, 0) AND OLD.vue_count IS NOT NULL THEN
        INSERT INTO evenement_statistique (id_espace, id_offre, type_evenement) VALUES (NEW.id_espace, NEW.id_offre, 'VUE_OFFRE');
    END IF;
    RETURN NULL;
END $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_evenement_vue_offre ON offre;
CREATE TRIGGER trg_evenement_vue_offre AFTER UPDATE OF vue_count ON offre
    FOR EACH ROW EXECUTE FUNCTION evenement_vue_offre();

CREATE OR REPLACE FUNCTION evenement_vue_espace() RETURNS TRIGGER AS $$
BEGIN
    IF COALESCE(NEW.nombre_vues, 0) > COALESCE(OLD.nombre_vues, 0) AND OLD.nombre_vues IS NOT NULL THEN
        INSERT INTO evenement_statistique (id_espace, type_evenement) VALUES (NEW.id_espace, 'VUE_ESPACE');
    END IF;
    RETURN NULL;
END $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_evenement_vue_espace ON espace_professionnel;
CREATE TRIGGER trg_evenement_vue_espace AFTER UPDATE OF nombre_vues ON espace_professionnel
    FOR EACH ROW EXECUTE FUNCTION evenement_vue_espace();
