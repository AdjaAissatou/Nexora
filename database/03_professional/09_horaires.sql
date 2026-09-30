-- ============================================================================
-- Horaires des espaces (docs/architecture-acteurs.md §10).
--
-- horaire : la semaine type, une ligne par jour (LUNDI … DIMANCHE) : fermé, ouvert
--   24 h/24, ou une plage [heure_ouverture, heure_fermeture) avec une pause
--   facultative. Une fermeture inférieure ou égale à l'ouverture veut dire que la
--   plage passe minuit (ex. 18:00 – 02:00).
-- horaire_exception : un jour précis qui remplace la semaine type (fermé, ou une
--   plage dans la journée).
-- Toutes les heures sont celles de Dakar (Africa/Dakar, UTC+0 sans heure d'été).
--
-- espace_ouvert_a(espace, moment) : vrai si l'espace est ouvert à ce moment,
-- faux sinon, NULL s'il n'a pas d'horaires. Même règle que la classe Java
-- sn.ucad.nexora.espace.domain.horaire.Horaires (mêmes cas testés des deux côtés).
--
-- Script rejouable.
-- ============================================================================

DO $$
BEGIN
    ALTER TABLE horaire ADD CONSTRAINT chk_horaire_jour
        CHECK (jour_semaine IN ('LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI', 'DIMANCHE'));
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_horaire_espace_jour ON horaire (id_espace, jour_semaine);
CREATE UNIQUE INDEX IF NOT EXISTS uq_horaire_exception_espace_date ON horaire_exception (id_espace, date_exception);

CREATE OR REPLACE FUNCTION jour_semaine_fr(d DATE) RETURNS VARCHAR
LANGUAGE sql IMMUTABLE AS $$
    SELECT (ARRAY['LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI', 'DIMANCHE'])[EXTRACT(ISODOW FROM d)::int];
$$;

CREATE OR REPLACE FUNCTION espace_ouvert_a(p_espace BIGINT, p_moment TIMESTAMP) RETURNS BOOLEAN
LANGUAGE plpgsql STABLE AS $$
DECLARE
    j DATE := p_moment::date;
    t TIME := p_moment::time;
    ex RECORD;
    h RECORD;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM horaire WHERE id_espace = p_espace) THEN
        RETURN NULL;
    END IF;

    -- 1. Le jour même : l'exception s'il y en a une, sinon la semaine type.
    SELECT * INTO ex FROM horaire_exception WHERE id_espace = p_espace AND date_exception = j;
    IF FOUND THEN
        IF NOT COALESCE(ex.ferme, FALSE) AND ex.heure_ouverture IS NOT NULL AND ex.heure_fermeture IS NOT NULL
           AND t >= ex.heure_ouverture AND t < ex.heure_fermeture THEN
            RETURN TRUE;
        END IF;
    ELSE
        SELECT * INTO h FROM horaire WHERE id_espace = p_espace AND jour_semaine = jour_semaine_fr(j);
        IF FOUND AND COALESCE(h.ouvert, FALSE) THEN
            IF COALESCE(h.ouvert_24h, FALSE) THEN
                RETURN TRUE;
            END IF;
            IF h.heure_ouverture IS NOT NULL AND h.heure_fermeture IS NOT NULL AND t >= h.heure_ouverture
               AND (h.heure_fermeture <= h.heure_ouverture OR t < h.heure_fermeture)
               AND NOT (h.pause_debut IS NOT NULL AND h.pause_fin IS NOT NULL AND t >= h.pause_debut AND t < h.pause_fin) THEN
                RETURN TRUE;
            END IF;
        END IF;
    END IF;

    -- 2. La nuit qui déborde de la veille (plage passant minuit), si la veille suit la semaine type.
    IF NOT EXISTS (SELECT 1 FROM horaire_exception WHERE id_espace = p_espace AND date_exception = j - 1) THEN
        SELECT * INTO h FROM horaire WHERE id_espace = p_espace AND jour_semaine = jour_semaine_fr(j - 1);
        IF FOUND AND COALESCE(h.ouvert, FALSE) AND NOT COALESCE(h.ouvert_24h, FALSE)
           AND h.heure_ouverture IS NOT NULL AND h.heure_fermeture IS NOT NULL
           AND h.heure_fermeture <= h.heure_ouverture AND t < h.heure_fermeture THEN
            RETURN TRUE;
        END IF;
    END IF;

    RETURN FALSE;
END $$;

COMMENT ON FUNCTION espace_ouvert_a(BIGINT, TIMESTAMP) IS
    'Vrai si l''espace est ouvert au moment donné (heure de Dakar), NULL sans horaires (§10)';
