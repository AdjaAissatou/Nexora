-- ============================================================================
-- Données de démonstration : 7 espaces professionnels avec compte, profil,
-- adresse et offres réelles (produits ou services selon l'activité).
-- Mot de passe pour tous les comptes ci-dessous : "Password1!"
-- (hash BCrypt réel, vérifié par une connexion réussie avant écriture de ce fichier)
-- Rejouable sans doublon : chaque bloc est gardé par ON CONFLICT / NOT EXISTS.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Auchan Discount Sénégal — Supermarché
-- ----------------------------------------------------------------------------

INSERT INTO accounts (first_name, last_name, email, phone, password, enabled, verified, locked, created_at)
VALUES ('Fatou', 'Diop', 'fatou.diop@nexora-demo.sn', '338601010',
        '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO utilisateurs (account_id, nom, prenom, email, telephone, email_verifie, telephone_verifie, langue, statut, actif)
SELECT a.id, a.last_name, a.first_name, a.email, a.phone, TRUE, TRUE, 'FRANCAIS', 'ACTIF', TRUE
FROM accounts a WHERE a.email = 'fatou.diop@nexora-demo.sn'
ON CONFLICT (email) DO NOTHING;

INSERT INTO espace_professionnel (id_utilisateur, id_type_espace, nom, slogan, description, telephone, email, ouvert, certifie, verifie, note_moyenne, nombre_avis, date_creation)
SELECT u.id_utilisateur, te.id_type_espace, 'Auchan Discount Sénégal', 'Vos courses au meilleur prix',
       'Enseigne de grande distribution proposant produits alimentaires, hygiène et entretien à prix discount.',
       '338601010', 'contact@auchan-demo.sn', TRUE, TRUE, TRUE, 4.30, 128, CURRENT_TIMESTAMP
FROM utilisateurs u, type_espace te
WHERE u.email = 'fatou.diop@nexora-demo.sn' AND te.nom = 'Supermarché'
AND NOT EXISTS (SELECT 1 FROM espace_professionnel WHERE nom = 'Auchan Discount Sénégal');

INSERT INTO adresse (id_espace, pays, region, departement, commune, quartier, adresse_complete, latitude, longitude, principale)
SELECT ep.id_espace, 'Sénégal', 'Dakar', 'Dakar', 'Dakar', 'Almadies', 'Route des Almadies, en face de la Foire de Dakar', 14.7444, -17.5133, TRUE
FROM espace_professionnel ep WHERE ep.nom = 'Auchan Discount Sénégal'
AND NOT EXISTS (SELECT 1 FROM adresse WHERE id_espace = ep.id_espace);

INSERT INTO offre (id_espace, id_type_offre, id_categorie, titre, description, prix, ancien_prix, disponible, est_commandable, stockable, statut, date_creation, date_publication)
SELECT ep.id_espace, tof.id_type_offre, tof.id_categorie, o.titre, o.description, o.prix, o.ancien_prix, TRUE, TRUE, TRUE, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM espace_professionnel ep
JOIN type_offre tof ON tof.libelle = 'Produits de vente'
CROSS JOIN (VALUES
    ('Riz parfumé Auchan 5kg', 'Riz parfumé de qualité supérieure, sac de 5kg.', 4500.00, NULL::numeric),
    ('Huile végétale Auchan 1L', 'Huile de table raffinée, bouteille de 1 litre.', 1200.00, NULL::numeric),
    ('Lait en poudre Auchan 900g', 'Lait entier en poudre, boîte de 900g.', 3200.00, 3600.00),
    ('Sucre en poudre 1kg', 'Sucre blanc cristallisé, paquet de 1kg.', 750.00, NULL::numeric),
    ('Pack eau minérale 1,5L x6', 'Pack de 6 bouteilles d''eau minérale de 1,5 litre.', 1800.00, NULL::numeric)
) AS o(titre, description, prix, ancien_prix)
WHERE ep.nom = 'Auchan Discount Sénégal'
AND NOT EXISTS (SELECT 1 FROM offre WHERE titre = o.titre AND id_espace = ep.id_espace);

INSERT INTO produit (id_offre, marque, quantite_stock, neuf)
SELECT o.id_offre, 'Auchan', s.stock, TRUE
FROM offre o
JOIN espace_professionnel ep ON ep.id_espace = o.id_espace AND ep.nom = 'Auchan Discount Sénégal'
JOIN (VALUES
    ('Riz parfumé Auchan 5kg', 200),
    ('Huile végétale Auchan 1L', 300),
    ('Lait en poudre Auchan 900g', 150),
    ('Sucre en poudre 1kg', 400),
    ('Pack eau minérale 1,5L x6', 250)
) AS s(titre, stock) ON s.titre = o.titre
WHERE NOT EXISTS (SELECT 1 FROM produit WHERE id_offre = o.id_offre);



-- ----------------------------------------------------------------------------
-- 2. Atelier Diagne Bois — Menuisier
-- ----------------------------------------------------------------------------

INSERT INTO accounts (first_name, last_name, email, phone, password, enabled, verified, locked, created_at)
VALUES ('Moussa', 'Diagne', 'moussa.diagne@nexora-demo.sn', '770300001',
        '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO utilisateurs (account_id, nom, prenom, email, telephone, email_verifie, telephone_verifie, langue, statut, actif)
SELECT a.id, a.last_name, a.first_name, a.email, a.phone, TRUE, TRUE, 'FRANCAIS', 'ACTIF', TRUE
FROM accounts a WHERE a.email = 'moussa.diagne@nexora-demo.sn'
ON CONFLICT (email) DO NOTHING;

INSERT INTO espace_professionnel (id_utilisateur, id_type_espace, nom, slogan, description, telephone, email, ouvert, certifie, verifie, note_moyenne, nombre_avis, date_creation)
SELECT u.id_utilisateur, te.id_type_espace, 'Atelier Diagne Bois', 'Meubles sur mesure et rénovation',
       'Menuisier artisanal : fabrication de mobilier sur mesure, pose de portes et fenêtres, rénovation de meubles anciens.',
       '770300001', 'contact@diagne-bois-demo.sn', TRUE, FALSE, TRUE, 4.60, 34, CURRENT_TIMESTAMP
FROM utilisateurs u, type_espace te
WHERE u.email = 'moussa.diagne@nexora-demo.sn' AND te.nom = 'Atelier'
AND NOT EXISTS (SELECT 1 FROM espace_professionnel WHERE nom = 'Atelier Diagne Bois');

INSERT INTO adresse (id_espace, pays, region, departement, commune, quartier, adresse_complete, latitude, longitude, principale)
SELECT ep.id_espace, 'Sénégal', 'Dakar', 'Pikine', 'Parcelles Assainies', 'Unité 12', 'Unité 12, Parcelles Assainies', 14.7833, -17.4167, TRUE
FROM espace_professionnel ep WHERE ep.nom = 'Atelier Diagne Bois'
AND NOT EXISTS (SELECT 1 FROM adresse WHERE id_espace = ep.id_espace);

INSERT INTO offre (id_espace, id_type_offre, id_categorie, titre, description, prix, negociable, disponible, est_commandable, est_reservable, statut, date_creation, date_publication)
SELECT ep.id_espace, tof.id_type_offre, tof.id_categorie, o.titre, o.description, o.prix, TRUE, TRUE, FALSE, TRUE, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM espace_professionnel ep
JOIN type_offre tof ON tof.libelle = 'Services professionnels'
CROSS JOIN (VALUES
    ('Fabrication de meubles sur mesure', 'Armoires, lits, tables et rangements fabriqués sur mesure selon vos plans.', 50000.00),
    ('Pose de portes et fenêtres en bois', 'Fourniture et pose de portes et fenêtres en bois massif.', 25000.00),
    ('Rénovation de mobilier ancien', 'Ponçage, réparation et vernissage de meubles anciens.', 15000.00)
) AS o(titre, description, prix)
WHERE ep.nom = 'Atelier Diagne Bois'
AND NOT EXISTS (SELECT 1 FROM offre WHERE titre = o.titre AND id_espace = ep.id_espace);

INSERT INTO service (id_offre, duree_estimee, intervention_domicile, delai_reponse, reservation)
SELECT o.id_offre, s.duree, TRUE, 120, TRUE
FROM offre o
JOIN espace_professionnel ep ON ep.id_espace = o.id_espace AND ep.nom = 'Atelier Diagne Bois'
JOIN (VALUES
    ('Fabrication de meubles sur mesure', 10080),
    ('Pose de portes et fenêtres en bois', 240),
    ('Rénovation de mobilier ancien', 1440)
) AS s(titre, duree) ON s.titre = o.titre
WHERE NOT EXISTS (SELECT 1 FROM service WHERE id_offre = o.id_offre);



-- ----------------------------------------------------------------------------
-- 3. SOS Plomberie Dakar — Plombier n°1
-- ----------------------------------------------------------------------------

INSERT INTO accounts (first_name, last_name, email, phone, password, enabled, verified, locked, created_at)
VALUES ('Ibrahima', 'Sarr', 'ibrahima.sarr@nexora-demo.sn', '770300002',
        '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO utilisateurs (account_id, nom, prenom, email, telephone, email_verifie, telephone_verifie, langue, statut, actif)
SELECT a.id, a.last_name, a.first_name, a.email, a.phone, TRUE, TRUE, 'FRANCAIS', 'ACTIF', TRUE
FROM accounts a WHERE a.email = 'ibrahima.sarr@nexora-demo.sn'
ON CONFLICT (email) DO NOTHING;

INSERT INTO espace_professionnel (id_utilisateur, id_type_espace, nom, slogan, description, telephone, email, ouvert, certifie, verifie, note_moyenne, nombre_avis, date_creation)
SELECT u.id_utilisateur, te.id_type_espace, 'SOS Plomberie Dakar', 'Dépannage rapide, 7j/7',
       'Plombier professionnel : dépannage d''urgence, installation sanitaire complète, disponible 7 jours sur 7.',
       '770300002', 'contact@sos-plomberie-demo.sn', TRUE, FALSE, TRUE, 4.10, 52, CURRENT_TIMESTAMP
FROM utilisateurs u, type_espace te
WHERE u.email = 'ibrahima.sarr@nexora-demo.sn' AND te.nom = 'Service'
AND NOT EXISTS (SELECT 1 FROM espace_professionnel WHERE nom = 'SOS Plomberie Dakar');

INSERT INTO adresse (id_espace, pays, region, departement, commune, quartier, adresse_complete, latitude, longitude, principale)
SELECT ep.id_espace, 'Sénégal', 'Dakar', 'Dakar', 'Médina', 'Médina', 'Avenue Blaise Diagne, Médina', 14.6833, -17.4500, TRUE
FROM espace_professionnel ep WHERE ep.nom = 'SOS Plomberie Dakar'
AND NOT EXISTS (SELECT 1 FROM adresse WHERE id_espace = ep.id_espace);

INSERT INTO offre (id_espace, id_type_offre, id_categorie, titre, description, prix, disponible, est_commandable, est_reservable, statut, date_creation, date_publication)
SELECT ep.id_espace, tof.id_type_offre, tof.id_categorie, o.titre, o.description, o.prix, TRUE, FALSE, TRUE, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM espace_professionnel ep
JOIN type_offre tof ON tof.libelle = 'Services professionnels'
CROSS JOIN (VALUES
    ('Dépannage plomberie urgence', 'Intervention rapide pour fuite, canalisation bouchée ou panne sanitaire.', 10000.00),
    ('Installation sanitaire complète', 'Installation complète de WC, lavabo, douche et robinetterie.', 75000.00)
) AS o(titre, description, prix)
WHERE ep.nom = 'SOS Plomberie Dakar'
AND NOT EXISTS (SELECT 1 FROM offre WHERE titre = o.titre AND id_espace = ep.id_espace);

INSERT INTO service (id_offre, duree_estimee, intervention_domicile, delai_reponse, reservation, urgence)
SELECT o.id_offre, s.duree, TRUE, s.delai, TRUE, s.urgence
FROM offre o
JOIN espace_professionnel ep ON ep.id_espace = o.id_espace AND ep.nom = 'SOS Plomberie Dakar'
JOIN (VALUES
    ('Dépannage plomberie urgence', 90, 30, TRUE),
    ('Installation sanitaire complète', 480, 180, FALSE)
) AS s(titre, duree, delai, urgence) ON s.titre = o.titre
WHERE NOT EXISTS (SELECT 1 FROM service WHERE id_offre = o.id_offre);



-- ----------------------------------------------------------------------------
-- 4. Plomberie Ndiaye & Fils — Plombier n°2
-- ----------------------------------------------------------------------------

INSERT INTO accounts (first_name, last_name, email, phone, password, enabled, verified, locked, created_at)
VALUES ('Cheikh', 'Ndiaye', 'cheikh.ndiaye@nexora-demo.sn', '770300003',
        '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO utilisateurs (account_id, nom, prenom, email, telephone, email_verifie, telephone_verifie, langue, statut, actif)
SELECT a.id, a.last_name, a.first_name, a.email, a.phone, TRUE, TRUE, 'FRANCAIS', 'ACTIF', TRUE
FROM accounts a WHERE a.email = 'cheikh.ndiaye@nexora-demo.sn'
ON CONFLICT (email) DO NOTHING;

INSERT INTO espace_professionnel (id_utilisateur, id_type_espace, nom, slogan, description, telephone, email, ouvert, certifie, verifie, note_moyenne, nombre_avis, date_creation)
SELECT u.id_utilisateur, te.id_type_espace, 'Plomberie Ndiaye & Fils', 'Le savoir-faire familial depuis 15 ans',
       'Entreprise familiale de plomberie : débouchage, installation de chauffe-eau et travaux sanitaires.',
       '770300003', 'contact@ndiaye-plomberie-demo.sn', TRUE, FALSE, FALSE, 3.90, 21, CURRENT_TIMESTAMP
FROM utilisateurs u, type_espace te
WHERE u.email = 'cheikh.ndiaye@nexora-demo.sn' AND te.nom = 'Service'
AND NOT EXISTS (SELECT 1 FROM espace_professionnel WHERE nom = 'Plomberie Ndiaye & Fils');

INSERT INTO adresse (id_espace, pays, region, departement, commune, quartier, adresse_complete, latitude, longitude, principale)
SELECT ep.id_espace, 'Sénégal', 'Dakar', 'Dakar', 'Grand Yoff', 'Grand Yoff', 'Route de Grand Yoff, non loin du marché', 14.7333, -17.4667, TRUE
FROM espace_professionnel ep WHERE ep.nom = 'Plomberie Ndiaye & Fils'
AND NOT EXISTS (SELECT 1 FROM adresse WHERE id_espace = ep.id_espace);

INSERT INTO offre (id_espace, id_type_offre, id_categorie, titre, description, prix, disponible, est_commandable, est_reservable, statut, date_creation, date_publication)
SELECT ep.id_espace, tof.id_type_offre, tof.id_categorie, o.titre, o.description, o.prix, TRUE, FALSE, TRUE, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM espace_professionnel ep
JOIN type_offre tof ON tof.libelle = 'Services professionnels'
CROSS JOIN (VALUES
    ('Débouchage canalisation', 'Débouchage de canalisations et évacuations bouchées.', 8000.00),
    ('Installation chauffe-eau', 'Fourniture et installation de chauffe-eau électrique ou solaire.', 35000.00)
) AS o(titre, description, prix)
WHERE ep.nom = 'Plomberie Ndiaye & Fils'
AND NOT EXISTS (SELECT 1 FROM offre WHERE titre = o.titre AND id_espace = ep.id_espace);

INSERT INTO service (id_offre, duree_estimee, intervention_domicile, delai_reponse, reservation, urgence)
SELECT o.id_offre, s.duree, TRUE, s.delai, TRUE, s.urgence
FROM offre o
JOIN espace_professionnel ep ON ep.id_espace = o.id_espace AND ep.nom = 'Plomberie Ndiaye & Fils'
JOIN (VALUES
    ('Débouchage canalisation', 60, 45, TRUE),
    ('Installation chauffe-eau', 180, 240, FALSE)
) AS s(titre, duree, delai, urgence) ON s.titre = o.titre
WHERE NOT EXISTS (SELECT 1 FROM service WHERE id_offre = o.id_offre);



-- ----------------------------------------------------------------------------
-- 5. Baye Transport — Chauffeur
-- ----------------------------------------------------------------------------

INSERT INTO accounts (first_name, last_name, email, phone, password, enabled, verified, locked, created_at)
VALUES ('Baye', 'Fall', 'baye.fall@nexora-demo.sn', '770300004',
        '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO utilisateurs (account_id, nom, prenom, email, telephone, email_verifie, telephone_verifie, langue, statut, actif)
SELECT a.id, a.last_name, a.first_name, a.email, a.phone, TRUE, TRUE, 'FRANCAIS', 'ACTIF', TRUE
FROM accounts a WHERE a.email = 'baye.fall@nexora-demo.sn'
ON CONFLICT (email) DO NOTHING;

INSERT INTO espace_professionnel (id_utilisateur, id_type_espace, nom, slogan, description, telephone, email, ouvert, certifie, verifie, note_moyenne, nombre_avis, date_creation)
SELECT u.id_utilisateur, te.id_type_espace, 'Baye Transport', 'Votre chauffeur de confiance à Dakar',
       'Chauffeur privé : courses en ville, location à la journée et livraison express de colis.',
       '770300004', 'contact@baye-transport-demo.sn', TRUE, FALSE, FALSE, 4.40, 67, CURRENT_TIMESTAMP
FROM utilisateurs u, type_espace te
WHERE u.email = 'baye.fall@nexora-demo.sn' AND te.nom = 'Service'
AND NOT EXISTS (SELECT 1 FROM espace_professionnel WHERE nom = 'Baye Transport');

INSERT INTO adresse (id_espace, pays, region, departement, commune, quartier, adresse_complete, latitude, longitude, principale)
SELECT ep.id_espace, 'Sénégal', 'Dakar', 'Dakar', 'Sicap Liberté', 'Sicap Liberté 6', 'Sicap Liberté 6, Dakar', 14.7167, -17.4500, TRUE
FROM espace_professionnel ep WHERE ep.nom = 'Baye Transport'
AND NOT EXISTS (SELECT 1 FROM adresse WHERE id_espace = ep.id_espace);

INSERT INTO offre (id_espace, id_type_offre, id_categorie, titre, description, prix, negociable, disponible, est_commandable, est_reservable, statut, date_creation, date_publication)
SELECT ep.id_espace, tof.id_type_offre, tof.id_categorie, o.titre, o.description, o.prix, o.negociable, TRUE, FALSE, TRUE, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM espace_professionnel ep
JOIN type_offre tof ON tof.libelle = 'Transport et livraison'
CROSS JOIN (VALUES
    ('Course VTC en ville (Dakar)', 'Déplacement en ville, prix à partir de ce montant selon la distance.', 3000.00, TRUE),
    ('Location chauffeur à la journée', 'Mise à disposition d''un chauffeur avec véhicule pour la journée.', 25000.00, FALSE),
    ('Livraison colis express', 'Livraison rapide de colis dans Dakar et sa banlieue.', 2000.00, TRUE)
) AS o(titre, description, prix, negociable)
WHERE ep.nom = 'Baye Transport'
AND NOT EXISTS (SELECT 1 FROM offre WHERE titre = o.titre AND id_espace = ep.id_espace);

INSERT INTO service (id_offre, intervention_domicile, delai_reponse, reservation)
SELECT o.id_offre, TRUE, 20, TRUE
FROM offre o
JOIN espace_professionnel ep ON ep.id_espace = o.id_espace AND ep.nom = 'Baye Transport'
WHERE NOT EXISTS (SELECT 1 FROM service WHERE id_offre = o.id_offre);



-- ----------------------------------------------------------------------------
-- 6. Boutique Sarah Mode — Magasin de vêtements
-- ----------------------------------------------------------------------------

INSERT INTO accounts (first_name, last_name, email, phone, password, enabled, verified, locked, created_at)
VALUES ('Sarah', 'Ba', 'sarah.ba@nexora-demo.sn', '770300005',
        '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO utilisateurs (account_id, nom, prenom, email, telephone, email_verifie, telephone_verifie, langue, statut, actif)
SELECT a.id, a.last_name, a.first_name, a.email, a.phone, TRUE, TRUE, 'FRANCAIS', 'ACTIF', TRUE
FROM accounts a WHERE a.email = 'sarah.ba@nexora-demo.sn'
ON CONFLICT (email) DO NOTHING;

INSERT INTO espace_professionnel (id_utilisateur, id_type_espace, nom, slogan, description, telephone, email, ouvert, certifie, verifie, note_moyenne, nombre_avis, date_creation)
SELECT u.id_utilisateur, te.id_type_espace, 'Boutique Sarah Mode', 'La mode africaine et occidentale à Dakar',
       'Boutique de prêt-à-porter : vêtements traditionnels et modernes pour homme et femme.',
       '770300005', 'contact@sarah-mode-demo.sn', TRUE, FALSE, TRUE, 4.50, 89, CURRENT_TIMESTAMP
FROM utilisateurs u, type_espace te
WHERE u.email = 'sarah.ba@nexora-demo.sn' AND te.nom = 'Boutique'
AND NOT EXISTS (SELECT 1 FROM espace_professionnel WHERE nom = 'Boutique Sarah Mode');

INSERT INTO adresse (id_espace, pays, region, departement, commune, quartier, adresse_complete, latitude, longitude, principale)
SELECT ep.id_espace, 'Sénégal', 'Dakar', 'Dakar', 'Plateau', 'Sandaga', 'Marché Sandaga, Plateau', 14.6708, -17.4364, TRUE
FROM espace_professionnel ep WHERE ep.nom = 'Boutique Sarah Mode'
AND NOT EXISTS (SELECT 1 FROM adresse WHERE id_espace = ep.id_espace);

INSERT INTO offre (id_espace, id_type_offre, id_categorie, titre, description, prix, negociable, disponible, est_commandable, stockable, statut, date_creation, date_publication)
SELECT ep.id_espace, tof.id_type_offre, tof.id_categorie, o.titre, o.description, o.prix, TRUE, TRUE, TRUE, TRUE, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM espace_professionnel ep
JOIN type_offre tof ON tof.libelle = 'Produits de vente'
CROSS JOIN (VALUES
    ('Robe wax élégante', 'Robe en pagne wax, coupe moderne, plusieurs coloris disponibles.', 15000.00),
    ('Chemise homme coton', 'Chemise en coton, coupe droite, plusieurs tailles.', 8500.00),
    ('Pantalon jean slim', 'Jean slim confortable, plusieurs coloris.', 12000.00),
    ('Ensemble boubou traditionnel', 'Boubou brodé pour homme, tissu de qualité.', 35000.00),
    ('Chaussures cuir homme', 'Chaussures en cuir véritable, plusieurs pointures.', 22000.00)
) AS o(titre, description, prix)
WHERE ep.nom = 'Boutique Sarah Mode'
AND NOT EXISTS (SELECT 1 FROM offre WHERE titre = o.titre AND id_espace = ep.id_espace);

INSERT INTO produit (id_offre, quantite_stock, neuf)
SELECT o.id_offre, s.stock, TRUE
FROM offre o
JOIN espace_professionnel ep ON ep.id_espace = o.id_espace AND ep.nom = 'Boutique Sarah Mode'
JOIN (VALUES
    ('Robe wax élégante', 25),
    ('Chemise homme coton', 40),
    ('Pantalon jean slim', 35),
    ('Ensemble boubou traditionnel', 12),
    ('Chaussures cuir homme', 18)
) AS s(titre, stock) ON s.titre = o.titre
WHERE NOT EXISTS (SELECT 1 FROM produit WHERE id_offre = o.id_offre);



-- ----------------------------------------------------------------------------
-- 7. TechnoPlus Informatique — Magasin de matériel informatique
-- ----------------------------------------------------------------------------

INSERT INTO accounts (first_name, last_name, email, phone, password, enabled, verified, locked, created_at)
VALUES ('Ousmane', 'Kane', 'ousmane.kane@nexora-demo.sn', '770300006',
        '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO utilisateurs (account_id, nom, prenom, email, telephone, email_verifie, telephone_verifie, langue, statut, actif)
SELECT a.id, a.last_name, a.first_name, a.email, a.phone, TRUE, TRUE, 'FRANCAIS', 'ACTIF', TRUE
FROM accounts a WHERE a.email = 'ousmane.kane@nexora-demo.sn'
ON CONFLICT (email) DO NOTHING;

INSERT INTO espace_professionnel (id_utilisateur, id_type_espace, nom, slogan, description, telephone, email, site_web, ouvert, certifie, verifie, note_moyenne, nombre_avis, date_creation)
SELECT u.id_utilisateur, te.id_type_espace, 'TechnoPlus Informatique', 'Votre spécialiste informatique à Dakar',
       'Vente d''ordinateurs, périphériques et accessoires informatiques, neufs et garantis.',
       '770300006', 'contact@technoplus-demo.sn', 'www.technoplus-demo.sn', TRUE, TRUE, TRUE, 4.70, 156, CURRENT_TIMESTAMP
FROM utilisateurs u, type_espace te
WHERE u.email = 'ousmane.kane@nexora-demo.sn' AND te.nom = 'Boutique'
AND NOT EXISTS (SELECT 1 FROM espace_professionnel WHERE nom = 'TechnoPlus Informatique');

INSERT INTO adresse (id_espace, pays, region, departement, commune, quartier, adresse_complete, latitude, longitude, principale)
SELECT ep.id_espace, 'Sénégal', 'Dakar', 'Dakar', 'Sacré-Cœur', 'Sacré-Cœur 3', 'Avenue Cheikh Anta Diop, Sacré-Cœur 3', 14.7000, -17.4667, TRUE
FROM espace_professionnel ep WHERE ep.nom = 'TechnoPlus Informatique'
AND NOT EXISTS (SELECT 1 FROM adresse WHERE id_espace = ep.id_espace);

INSERT INTO offre (id_espace, id_type_offre, id_categorie, titre, description, prix, ancien_prix, negociable, disponible, est_commandable, stockable, statut, date_creation, date_publication)
SELECT ep.id_espace, tof.id_type_offre, tof.id_categorie, o.titre, o.description, o.prix, o.ancien_prix, FALSE, TRUE, TRUE, TRUE, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM espace_professionnel ep
JOIN type_offre tof ON tof.libelle = 'Produits informatiques'
CROSS JOIN (VALUES
    ('Ordinateur portable HP Pavilion 15', 'PC portable 15,6 pouces, Intel Core i5, 8 Go RAM, 512 Go SSD.', 385000.00, 420000.00),
    ('PC de bureau Dell OptiPlex 3090', 'Unité centrale Intel Core i5, 8 Go RAM, 256 Go SSD, sans écran.', 320000.00, NULL::numeric),
    ('Imprimante Canon Pixma G2010', 'Imprimante jet d''encre couleur avec réservoirs rechargeables.', 65000.00, NULL::numeric),
    ('Souris sans fil Logitech M185', 'Souris optique sans fil, autonomie 12 mois.', 8500.00, NULL::numeric),
    ('Clavier mécanique gaming Redragon K552', 'Clavier mécanique rétroéclairé, switches bleus.', 18500.00, NULL::numeric),
    ('Disque dur externe Seagate 1To', 'Disque dur externe USB 3.0, 1 To de stockage.', 42000.00, NULL::numeric),
    ('Écran LED 24 pouces Samsung S24F350', 'Moniteur Full HD 24 pouces, dalle incurvée.', 95000.00, 105000.00)
) AS o(titre, description, prix, ancien_prix)
WHERE ep.nom = 'TechnoPlus Informatique'
AND NOT EXISTS (SELECT 1 FROM offre WHERE titre = o.titre AND id_espace = ep.id_espace);

INSERT INTO produit (id_offre, marque, modele, reference, quantite_stock, poids, garantie, neuf)
SELECT o.id_offre, d.marque, d.modele, d.reference, d.stock, d.poids, d.garantie, TRUE
FROM offre o
JOIN espace_professionnel ep ON ep.id_espace = o.id_espace AND ep.nom = 'TechnoPlus Informatique'
JOIN (VALUES
    ('Ordinateur portable HP Pavilion 15', 'HP', 'Pavilion 15-eg2001sn', 'HP-PAV15-2024', 15, 1.75, '12 mois'),
    ('PC de bureau Dell OptiPlex 3090', 'Dell', 'OptiPlex 3090 MT', 'DELL-OPT3090', 8, 5.20, '24 mois'),
    ('Imprimante Canon Pixma G2010', 'Canon', 'Pixma G2010', 'CANON-G2010', 20, 4.00, '12 mois'),
    ('Souris sans fil Logitech M185', 'Logitech', 'M185', 'LOGI-M185', 60, 0.09, '6 mois'),
    ('Clavier mécanique gaming Redragon K552', 'Redragon', 'K552 Kumara', 'RD-K552', 25, 0.85, '12 mois'),
    ('Disque dur externe Seagate 1To', 'Seagate', 'Expansion 1TB', 'SG-EXP1TB', 30, 0.20, '24 mois'),
    ('Écran LED 24 pouces Samsung S24F350', 'Samsung', 'S24F350FHU', 'SAM-S24F350', 12, 3.10, '12 mois')
) AS d(titre, marque, modele, reference, stock, poids, garantie) ON d.titre = o.titre
WHERE NOT EXISTS (SELECT 1 FROM produit WHERE id_offre = o.id_offre);

-- ----------------------------------------------------------------------------
-- Images : une photo réellement représentative par produit/service (pas de
-- placeholder aléatoire) — sources Unsplash et Wikimedia Commons, vérifiées
-- visuellement une à une avant intégration ici.
-- ----------------------------------------------------------------------------

INSERT INTO image (id_offre, url, principale, ordre_affichage, texte_alternatif)
SELECT o.id_offre, m.url, TRUE, 0, o.titre
FROM offre o
JOIN (VALUES
    ('Riz parfumé Auchan 5kg', 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=640&h=480&fit=crop&q=75'),
    ('Huile végétale Auchan 1L', 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=640&h=480&fit=crop&q=75'),
    ('Lait en poudre Auchan 900g', 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=640&h=480&fit=crop&q=75'),
    ('Sucre en poudre 1kg', 'https://commons.wikimedia.org/wiki/Special:FilePath/W%C3%BCrfelzucker%20--%202018%20--%203564.jpg?width=640'),
    ('Pack eau minérale 1,5L x6', 'https://images.unsplash.com/photo-1523362628745-0c100150b504?w=640&h=480&fit=crop&q=75'),

    ('Fabrication de meubles sur mesure', 'https://images.unsplash.com/photo-1601058268499-e52658b8bb88?w=640&h=480&fit=crop&q=75'),
    ('Pose de portes et fenêtres en bois', 'https://images.unsplash.com/photo-1509644851169-2acc08aa25b5?w=640&h=480&fit=crop&q=75'),
    ('Rénovation de mobilier ancien', 'https://images.unsplash.com/photo-1581539250439-c96689b516dd?w=640&h=480&fit=crop&q=75'),

    ('Dépannage plomberie urgence', 'https://images.unsplash.com/photo-1607472586893-edb57bdc0e39?w=640&h=480&fit=crop&q=75'),
    ('Installation sanitaire complète', 'https://images.unsplash.com/photo-1607472586893-edb57bdc0e39?w=640&h=480&fit=crop&q=75'),
    ('Débouchage canalisation', 'https://images.unsplash.com/photo-1607472586893-edb57bdc0e39?w=640&h=480&fit=crop&q=75'),
    ('Installation chauffe-eau', 'https://commons.wikimedia.org/wiki/Special:FilePath/Rheem%20home%20gas%20water%20heater%20tank%20-%20newly%20installed.jpg?width=640'),

    ('Course VTC en ville (Dakar)', 'https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=640&h=480&fit=crop&q=75'),
    ('Location chauffeur à la journée', 'https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=640&h=480&fit=crop&q=75'),
    ('Livraison colis express', 'https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=640&h=480&fit=crop&q=75'),

    ('Robe wax élégante', 'https://images.unsplash.com/photo-1596783074918-c84cb06531ca?w=640&h=480&fit=crop&q=75'),
    ('Chemise homme coton', 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=640&h=480&fit=crop&q=75'),
    ('Pantalon jean slim', 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=640&h=480&fit=crop&q=75'),
    ('Ensemble boubou traditionnel', 'https://commons.wikimedia.org/wiki/Special:FilePath/Boubou%20traditionnel%20du%20S%C3%A9n%C3%A9gal.jpg?width=640'),
    ('Chaussures cuir homme', 'https://commons.wikimedia.org/wiki/Special:FilePath/Mens%20brown%20derby%20leather%20shoes.jpg?width=640'),

    ('Ordinateur portable HP Pavilion 15', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=640&h=480&fit=crop&q=75'),
    ('PC de bureau Dell OptiPlex 3090', 'https://images.unsplash.com/photo-1587831990711-23ca6441447b?w=640&h=480&fit=crop&q=75'),
    ('Imprimante Canon Pixma G2010', 'https://images.unsplash.com/photo-1612815154858-60aa4c59eaa6?w=640&h=480&fit=crop&q=75'),
    ('Souris sans fil Logitech M185', 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=640&h=480&fit=crop&q=75'),
    ('Clavier mécanique gaming Redragon K552', 'https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=640&h=480&fit=crop&q=75'),
    ('Disque dur externe Seagate 1To', 'https://commons.wikimedia.org/wiki/Special:FilePath/35-Desktop-Hard-Drive.jpg?width=640'),
    ('Écran LED 24 pouces Samsung S24F350', 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=640&h=480&fit=crop&q=75')
) AS m(titre, url) ON m.titre = o.titre
WHERE NOT EXISTS (SELECT 1 FROM image WHERE id_offre = o.id_offre);

-- Promotion active sur l'ordinateur portable HP (en cours, visible sur la recherche)
INSERT INTO promotion (id_offre, nom, description, type_reduction, valeur, date_debut, date_fin, actif)
SELECT o.id_offre, 'Promo rentrée', 'Réduction de rentrée sur les ordinateurs portables.', 'MONTANT_FIXE', 35000.00,
       CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '28 days', TRUE
FROM offre o
JOIN espace_professionnel ep ON ep.id_espace = o.id_espace AND ep.nom = 'TechnoPlus Informatique'
WHERE o.titre = 'Ordinateur portable HP Pavilion 15'
AND NOT EXISTS (SELECT 1 FROM promotion WHERE id_offre = o.id_offre);
