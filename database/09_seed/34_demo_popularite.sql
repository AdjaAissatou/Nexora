-- ============================================================================
-- Démonstration (docs/architecture-acteurs.md §23) : vues et « J'aime » dans Découvrir, venant de
-- visiteurs fictifs (« demo-… »), sur les offres des espaces de démonstration seulement (comptes
-- @nexora-demo.sn) : les offres des vrais comptes ne sont jamais touchées. Quantités fixes selon
-- l'offre, quelques-unes nettement plus aimées : elles portent le badge « 🔥 Populaire ».
-- Les compteurs suivent par les déclencheurs de 11_migrations/11_popularite_offres.sql.
-- Rejouable : ne fait rien si les visiteurs de démonstration existent déjà.
-- ============================================================================

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM decouverte_signal WHERE visiteur LIKE 'demo-%') THEN RETURN; END IF;

    -- Cartes regardées : de 3 à 27 visiteurs par offre, sur les 40 derniers jours
    INSERT INTO decouverte_signal (visiteur, id_offre, type_signal, duree_ms, date_signal)
    SELECT 'demo-' || g, o.id_offre, CASE WHEN g % 3 = 0 THEN 'VUE_LONGUE' ELSE 'VUE' END,
           CASE WHEN g % 3 = 0 THEN 4000 + g * 97 % 6000 ELSE 1200 + g * 137 % 2000 END,
           NOW() - (g * 3 % 40) * INTERVAL '1 day'
    FROM offre o
    JOIN espace_professionnel e ON e.id_espace = o.id_espace
    JOIN utilisateurs u ON u.id_utilisateur = e.id_utilisateur AND u.email LIKE '%@nexora-demo.sn'
    CROSS JOIN LATERAL generate_series(1, 3 + (o.id_offre * 7 % 25)) g
    WHERE CAST(o.statut AS TEXT) = 'PUBLIE';

    -- « J'aime » : 0 à 5 par offre, et une offre sur six bien plus aimée (+9)
    INSERT INTO decouverte_signal (visiteur, id_offre, type_signal, date_signal)
    SELECT 'demo-' || g, o.id_offre, 'J_AIME', NOW() - (g * 5 % 30) * INTERVAL '1 day'
    FROM offre o
    JOIN espace_professionnel e ON e.id_espace = o.id_espace
    JOIN utilisateurs u ON u.id_utilisateur = e.id_utilisateur AND u.email LIKE '%@nexora-demo.sn'
    CROSS JOIN LATERAL generate_series(1, (o.id_offre * 13 % 6) + CASE WHEN o.id_offre % 6 = 0 THEN 9 ELSE 0 END) g
    WHERE CAST(o.statut AS TEXT) = 'PUBLIE';
END $$;
