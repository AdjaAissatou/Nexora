-- ============================================================================
-- DÉMONSTRATION (hors install.sql) : horaires des espaces de démo (§10).
-- Rejouable : remplace la semaine type des espaces cités, sans toucher aux autres.
-- ============================================================================

DELETE FROM horaire h USING espace_professionnel e
WHERE h.id_espace = e.id_espace
  AND e.nom IN ('Auchan Discount Sénégal', 'Atelier Diagne Bois', 'SOS Plomberie Dakar', 'Plomberie Ndiaye & Fils',
                'Baye Transport', 'Boutique Sarah Mode', 'TechnoPlus Informatique');

INSERT INTO horaire (id_espace, jour_semaine, ouvert, ouvert_24h, heure_ouverture, heure_fermeture, pause_debut, pause_fin)
SELECT e.id_espace, v.jour, v.ouvert, v.h24, v.ouv::time, v.ferm::time, v.pd::time, v.pf::time
FROM (VALUES
    -- Supermarché : tous les jours, dimanche matin seulement
    ('Auchan Discount Sénégal', 'LUNDI', TRUE, FALSE, '08:00', '22:00', NULL, NULL),
    ('Auchan Discount Sénégal', 'MARDI', TRUE, FALSE, '08:00', '22:00', NULL, NULL),
    ('Auchan Discount Sénégal', 'MERCREDI', TRUE, FALSE, '08:00', '22:00', NULL, NULL),
    ('Auchan Discount Sénégal', 'JEUDI', TRUE, FALSE, '08:00', '22:00', NULL, NULL),
    ('Auchan Discount Sénégal', 'VENDREDI', TRUE, FALSE, '08:00', '22:00', NULL, NULL),
    ('Auchan Discount Sénégal', 'SAMEDI', TRUE, FALSE, '08:00', '22:00', NULL, NULL),
    ('Auchan Discount Sénégal', 'DIMANCHE', TRUE, FALSE, '09:00', '13:00', NULL, NULL),
    -- Atelier : semaine avec pause, fermé le vendredi après-midi (prière) et le dimanche
    ('Atelier Diagne Bois', 'LUNDI', TRUE, FALSE, '08:00', '18:00', '13:00', '14:30'),
    ('Atelier Diagne Bois', 'MARDI', TRUE, FALSE, '08:00', '18:00', '13:00', '14:30'),
    ('Atelier Diagne Bois', 'MERCREDI', TRUE, FALSE, '08:00', '18:00', '13:00', '14:30'),
    ('Atelier Diagne Bois', 'JEUDI', TRUE, FALSE, '08:00', '18:00', '13:00', '14:30'),
    ('Atelier Diagne Bois', 'VENDREDI', TRUE, FALSE, '08:00', '13:00', NULL, NULL),
    ('Atelier Diagne Bois', 'SAMEDI', TRUE, FALSE, '09:00', '14:00', NULL, NULL),
    ('Atelier Diagne Bois', 'DIMANCHE', FALSE, FALSE, NULL, NULL, NULL, NULL),
    -- Dépannage d'urgence : 24 h/24
    ('SOS Plomberie Dakar', 'LUNDI', TRUE, TRUE, NULL, NULL, NULL, NULL),
    ('SOS Plomberie Dakar', 'MARDI', TRUE, TRUE, NULL, NULL, NULL, NULL),
    ('SOS Plomberie Dakar', 'MERCREDI', TRUE, TRUE, NULL, NULL, NULL, NULL),
    ('SOS Plomberie Dakar', 'JEUDI', TRUE, TRUE, NULL, NULL, NULL, NULL),
    ('SOS Plomberie Dakar', 'VENDREDI', TRUE, TRUE, NULL, NULL, NULL, NULL),
    ('SOS Plomberie Dakar', 'SAMEDI', TRUE, TRUE, NULL, NULL, NULL, NULL),
    ('SOS Plomberie Dakar', 'DIMANCHE', TRUE, TRUE, NULL, NULL, NULL, NULL),
    -- Plomberie familiale
    ('Plomberie Ndiaye & Fils', 'LUNDI', TRUE, FALSE, '08:30', '18:30', '13:00', '15:00'),
    ('Plomberie Ndiaye & Fils', 'MARDI', TRUE, FALSE, '08:30', '18:30', '13:00', '15:00'),
    ('Plomberie Ndiaye & Fils', 'MERCREDI', TRUE, FALSE, '08:30', '18:30', '13:00', '15:00'),
    ('Plomberie Ndiaye & Fils', 'JEUDI', TRUE, FALSE, '08:30', '18:30', '13:00', '15:00'),
    ('Plomberie Ndiaye & Fils', 'VENDREDI', TRUE, FALSE, '08:30', '13:00', NULL, NULL),
    ('Plomberie Ndiaye & Fils', 'SAMEDI', TRUE, FALSE, '09:00', '13:00', NULL, NULL),
    ('Plomberie Ndiaye & Fils', 'DIMANCHE', FALSE, FALSE, NULL, NULL, NULL, NULL),
    -- Transport : tôt le matin, tard le soir, la nuit du samedi jusqu'à 2 h
    ('Baye Transport', 'LUNDI', TRUE, FALSE, '06:00', '23:00', NULL, NULL),
    ('Baye Transport', 'MARDI', TRUE, FALSE, '06:00', '23:00', NULL, NULL),
    ('Baye Transport', 'MERCREDI', TRUE, FALSE, '06:00', '23:00', NULL, NULL),
    ('Baye Transport', 'JEUDI', TRUE, FALSE, '06:00', '23:00', NULL, NULL),
    ('Baye Transport', 'VENDREDI', TRUE, FALSE, '06:00', '23:00', NULL, NULL),
    ('Baye Transport', 'SAMEDI', TRUE, FALSE, '06:00', '02:00', NULL, NULL),
    ('Baye Transport', 'DIMANCHE', TRUE, FALSE, '08:00', '20:00', NULL, NULL),
    -- Boutique de mode : l'après-midi et le soir
    ('Boutique Sarah Mode', 'LUNDI', FALSE, FALSE, NULL, NULL, NULL, NULL),
    ('Boutique Sarah Mode', 'MARDI', TRUE, FALSE, '10:00', '20:00', NULL, NULL),
    ('Boutique Sarah Mode', 'MERCREDI', TRUE, FALSE, '10:00', '20:00', NULL, NULL),
    ('Boutique Sarah Mode', 'JEUDI', TRUE, FALSE, '10:00', '20:00', NULL, NULL),
    ('Boutique Sarah Mode', 'VENDREDI', TRUE, FALSE, '10:00', '20:00', '13:30', '15:00'),
    ('Boutique Sarah Mode', 'SAMEDI', TRUE, FALSE, '10:00', '21:00', NULL, NULL),
    ('Boutique Sarah Mode', 'DIMANCHE', TRUE, FALSE, '15:00', '20:00', NULL, NULL),
    -- Informatique
    ('TechnoPlus Informatique', 'LUNDI', TRUE, FALSE, '09:00', '19:00', NULL, NULL),
    ('TechnoPlus Informatique', 'MARDI', TRUE, FALSE, '09:00', '19:00', NULL, NULL),
    ('TechnoPlus Informatique', 'MERCREDI', TRUE, FALSE, '09:00', '19:00', NULL, NULL),
    ('TechnoPlus Informatique', 'JEUDI', TRUE, FALSE, '09:00', '19:00', NULL, NULL),
    ('TechnoPlus Informatique', 'VENDREDI', TRUE, FALSE, '09:00', '19:00', '13:00', '15:00'),
    ('TechnoPlus Informatique', 'SAMEDI', TRUE, FALSE, '10:00', '18:00', NULL, NULL),
    ('TechnoPlus Informatique', 'DIMANCHE', FALSE, FALSE, NULL, NULL, NULL, NULL)
) AS v(espace, jour, ouvert, h24, ouv, ferm, pd, pf)
JOIN espace_professionnel e ON e.nom = v.espace;
