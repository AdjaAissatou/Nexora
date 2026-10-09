-- ============================================================================
-- Démonstration (docs/architecture-acteurs.md §26) : historique daté des vues de fiches et des clics
-- (Appeler, WhatsApp, Itinéraire, Partager) sur les 25 dernières semaines, pour les espaces des comptes
-- de démonstration (@nexora-demo.sn) seulement. Autant d'événements « vue » que les compteurs actuels,
-- plus nombreux ces dernières semaines ; quelques clics par offre ; des favoris datés posés par les comptes
-- de démonstration sur les offres des autres. Visiteur « demo-stat ».
-- Rejouable : ne fait rien si cet historique existe déjà.
-- ============================================================================

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM evenement_statistique WHERE visiteur = 'demo-stat') THEN RETURN; END IF;

    -- Sur une installation neuve, les offres de démonstration n'ont encore aucune vue : on leur en donne
    -- un socle, pour que compteurs et historique concordent. Les déclencheurs de vues ajoutent alors un
    -- événement daté de maintenant par ligne modifiée : on les retire (même horodatage de transaction).
    UPDATE offre o SET vue_count = 25 + (o.id_offre * 37) % 60
    FROM espace_professionnel e JOIN utilisateurs u ON u.id_utilisateur = e.id_utilisateur AND u.email LIKE '%@nexora-demo.sn'
    WHERE e.id_espace = o.id_espace AND COALESCE(o.vue_count, 0) < 25 + (o.id_offre * 37) % 60;
    UPDATE espace_professionnel e SET nombre_vues = 40 + (e.id_espace * 13) % 80
    FROM utilisateurs u
    WHERE u.id_utilisateur = e.id_utilisateur AND u.email LIKE '%@nexora-demo.sn' AND COALESCE(e.nombre_vues, 0) < 40 + (e.id_espace * 13) % 80;
    DELETE FROM evenement_statistique WHERE visiteur IS NULL AND date_evenement = NOW();

    -- Vues des fiches d'offres : vue_count événements, plus denses récemment
    INSERT INTO evenement_statistique (id_espace, id_offre, type_evenement, visiteur, date_evenement)
    SELECT o.id_espace, o.id_offre, 'VUE_OFFRE', 'demo-stat',
           NOW() - (FLOOR(175 * POWER(((g * 53 + o.id_offre * 7) % 97) / 97.0, 1.6)) || ' days')::INTERVAL
                 - ((g * 13 % 24) || ' hours')::INTERVAL
    FROM offre o
    JOIN espace_professionnel e ON e.id_espace = o.id_espace
    JOIN utilisateurs u ON u.id_utilisateur = e.id_utilisateur AND u.email LIKE '%@nexora-demo.sn'
    CROSS JOIN LATERAL generate_series(1, LEAST(COALESCE(o.vue_count, 0), 400)) g;

    -- Vues des fiches d'espaces
    INSERT INTO evenement_statistique (id_espace, type_evenement, visiteur, date_evenement)
    SELECT e.id_espace, 'VUE_ESPACE', 'demo-stat',
           NOW() - (FLOOR(175 * POWER(((g * 59 + e.id_espace * 11) % 97) / 97.0, 1.5)) || ' days')::INTERVAL
    FROM espace_professionnel e
    JOIN utilisateurs u ON u.id_utilisateur = e.id_utilisateur AND u.email LIKE '%@nexora-demo.sn'
    CROSS JOIN LATERAL generate_series(1, LEAST(COALESCE(e.nombre_vues, 0), 600)) g;

    -- Clics : environ une vue sur huit mène à un contact
    INSERT INTO evenement_statistique (id_espace, id_offre, type_evenement, visiteur, date_evenement)
    SELECT o.id_espace, o.id_offre,
           (ARRAY['APPEL', 'WHATSAPP', 'WHATSAPP', 'ITINERAIRE', 'PARTAGE'])[1 + (g + o.id_offre) % 5], 'demo-stat',
           NOW() - (FLOOR(175 * POWER(((g * 31 + o.id_offre * 5) % 89) / 89.0, 1.6)) || ' days')::INTERVAL
    FROM offre o
    JOIN espace_professionnel e ON e.id_espace = o.id_espace
    JOIN utilisateurs u ON u.id_utilisateur = e.id_utilisateur AND u.email LIKE '%@nexora-demo.sn'
    CROSS JOIN LATERAL generate_series(1, COALESCE(o.vue_count, 0) / 8) g;

    -- Favoris : chaque compte de démonstration enregistre quelques offres des autres espaces
    -- (le déclencheur trg_favori_compter_offre tient offre.nombre_favoris à jour)
    INSERT INTO favori (id_utilisateur, id_offre, date_creation)
    SELECT u.id_utilisateur, o.id_offre,
           NOW() - (((o.id_offre * 3 + u.id_utilisateur * 5) % 150) || ' days')::INTERVAL
                 - (((o.id_offre + u.id_utilisateur) % 20) || ' hours')::INTERVAL
    FROM utilisateurs u
    JOIN offre o ON (o.id_offre * 7 + u.id_utilisateur * 13) % 17 = 0
    JOIN espace_professionnel e ON e.id_espace = o.id_espace AND e.id_utilisateur <> u.id_utilisateur
    WHERE u.email LIKE '%@nexora-demo.sn'
      AND CAST(o.statut AS TEXT) = 'PUBLIE'
      AND NOT EXISTS (SELECT 1 FROM favori f WHERE f.id_utilisateur = u.id_utilisateur AND f.id_offre = o.id_offre);
END $$;
