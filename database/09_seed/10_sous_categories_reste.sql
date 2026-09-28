-- Complète la catégorisation hiérarchique pour les 8 catégories racines
-- restantes (Restauration, Beauté, Transport, Immobilier, Éducation,
-- Services, Hôtellerie, Administration) — même principe que 08 et 09 :
-- sous-catégories + types d'offre précis, pour que la personne qui publie
-- une offre choisisse dans une liste réaliste plutôt que de taper un titre
-- depuis zéro. Idempotent (INSERT ... WHERE NOT EXISTS / ON CONFLICT).

-------------------------------------------------------
-- RESTAURATION
-------------------------------------------------------

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, v.nom, v.description, v.icone, '#f59e0b', v.ordre, TRUE, CURRENT_TIMESTAMP
FROM categorie, (VALUES
    ('Entrées', 'Pastels, salades, accras...', 'utensils', 1),
    ('Plats', 'Plats sénégalais et internationaux', 'utensils', 2),
    ('Desserts', 'Desserts et pâtisseries', 'cake', 3),
    ('Boissons', 'Jus, boissons chaudes et fraîches', 'cup-soda', 4)
) AS v(nom, description, icone, ordre)
WHERE categorie.nom = 'Restauration'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'PRODUIT', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Pastels', 'Beignets farcis au poisson ou à la viande'),
    ('Salade', 'Salade composée'),
    ('Accras', 'Beignets de poisson pimentés')
) AS v(libelle, description)
WHERE c.nom = 'Entrées' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'PRODUIT', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Thiéboudienne', 'Riz au poisson, plat national sénégalais'),
    ('Yassa poulet', 'Poulet mariné aux oignons et citron'),
    ('Mafé', 'Viande en sauce arachide'),
    ('Thiou', 'Sauce tomate épicée avec viande ou poisson'),
    ('Soupou kandia', 'Sauce gombo')
) AS v(libelle, description)
WHERE c.nom = 'Plats' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'PRODUIT', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Thiakry', 'Dessert au mil et lait caillé'),
    ('Salade de fruits', 'Fruits frais de saison'),
    ('Beignets sucrés', 'Beignets traditionnels')
) AS v(libelle, description)
WHERE c.nom = 'Desserts' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'PRODUIT', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Bissap', 'Jus d''hibiscus'),
    ('Bouye', 'Jus de fruit de baobab'),
    ('Gingembre', 'Jus de gingembre'),
    ('Café Touba', 'Café épicé sénégalais'),
    ('Jus de fruits', 'Jus de fruits frais')
) AS v(libelle, description)
WHERE c.nom = 'Boissons' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

-------------------------------------------------------
-- BEAUTÉ
-------------------------------------------------------

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, v.nom, v.description, v.icone, '#ec4899', v.ordre, TRUE, CURRENT_TIMESTAMP
FROM categorie, (VALUES
    ('Coiffure', 'Coupe, tresses, coloration', 'scissors', 1),
    ('Esthétique', 'Soins du visage et des ongles', 'sparkles', 2),
    ('Bien-être', 'Massage et épilation', 'flower', 3)
) AS v(nom, description, icone, ordre)
WHERE categorie.nom = 'Beauté'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Coupe homme', 'Coupe et entretien de cheveux homme'),
    ('Coupe femme', 'Coupe et coiffage femme'),
    ('Tresses', 'Tresses africaines, nattes'),
    ('Défrisage', 'Défrisage et lissage'),
    ('Coloration', 'Coloration et mèches')
) AS v(libelle, description)
WHERE c.nom = 'Coiffure' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Manucure', 'Soin et pose d''ongles mains'),
    ('Pédicure', 'Soin des pieds'),
    ('Soin du visage', 'Nettoyage et soin de la peau'),
    ('Maquillage', 'Maquillage événementiel')
) AS v(libelle, description)
WHERE c.nom = 'Esthétique' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Massage', 'Massage relaxant ou thérapeutique'),
    ('Épilation', 'Épilation à la cire ou au fil')
) AS v(libelle, description)
WHERE c.nom = 'Bien-être' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

-------------------------------------------------------
-- TRANSPORT
-------------------------------------------------------

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, v.nom, v.description, v.icone, '#0f766e', v.ordre, TRUE, CURRENT_TIMESTAMP
FROM categorie, (VALUES
    ('Déplacement', 'Courses et trajets', 'car', 1),
    ('Livraison', 'Livraison de colis et repas', 'package', 2),
    ('Location de véhicule', 'Location avec ou sans chauffeur', 'car-front', 3)
) AS v(nom, description, icone, ordre)
WHERE categorie.nom = 'Transport'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Course en ville', 'Déplacement urbain'),
    ('Trajet aéroport', 'Transfert vers ou depuis l''aéroport'),
    ('Trajet longue distance', 'Trajet inter-villes')
) AS v(libelle, description)
WHERE c.nom = 'Déplacement' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Livraison de colis', 'Livraison rapide de colis'),
    ('Livraison de repas', 'Livraison de commandes de restaurants')
) AS v(libelle, description)
WHERE c.nom = 'Livraison' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Location avec chauffeur', 'Véhicule et chauffeur à la journée'),
    ('Location sans chauffeur', 'Location de véhicule seul')
) AS v(libelle, description)
WHERE c.nom = 'Location de véhicule' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

-------------------------------------------------------
-- IMMOBILIER
-------------------------------------------------------

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, v.nom, v.description, v.icone, '#7c3aed', v.ordre, TRUE, CURRENT_TIMESTAMP
FROM categorie, (VALUES
    ('Location immobilière', 'Biens à louer', 'key', 1),
    ('Vente immobilière', 'Biens à vendre', 'home', 2)
) AS v(nom, description, icone, ordre)
WHERE categorie.nom = 'Immobilier'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'PRODUIT', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Appartement à louer', 'Appartement en location'),
    ('Maison à louer', 'Maison en location'),
    ('Studio à louer', 'Studio meublé ou non'),
    ('Chambre à louer', 'Chambre simple en colocation'),
    ('Bureau à louer', 'Espace de bureau professionnel')
) AS v(libelle, description)
WHERE c.nom = 'Location immobilière' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'PRODUIT', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Appartement à vendre', 'Appartement en vente'),
    ('Maison à vendre', 'Maison en vente'),
    ('Terrain à vendre', 'Terrain nu ou viabilisé'),
    ('Local commercial à vendre', 'Local pour activité commerciale')
) AS v(libelle, description)
WHERE c.nom = 'Vente immobilière' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

-------------------------------------------------------
-- ÉDUCATION
-------------------------------------------------------

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, v.nom, v.description, v.icone, '#0891b2', v.ordre, TRUE, CURRENT_TIMESTAMP
FROM categorie, (VALUES
    ('Cours particuliers', 'Soutien scolaire et cours individuels', 'book-open', 1),
    ('Formations', 'Formations professionnelles et en ligne', 'graduation-cap', 2)
) AS v(nom, description, icone, ordre)
WHERE categorie.nom = 'Éducation'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Soutien scolaire primaire', 'Cours pour le primaire'),
    ('Soutien scolaire secondaire', 'Cours pour le collège et lycée'),
    ('Cours de langues', 'Français, anglais, arabe...'),
    ('Cours d''informatique', 'Bureautique et programmation'),
    ('Cours de musique', 'Instrument ou chant')
) AS v(libelle, description)
WHERE c.nom = 'Cours particuliers' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Formation professionnelle', 'Formation qualifiante dans un métier'),
    ('Formation en ligne', 'Cours à distance'),
    ('Préparation aux concours', 'Préparation à un examen ou concours')
) AS v(libelle, description)
WHERE c.nom = 'Formations' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

-------------------------------------------------------
-- SERVICES (divers)
-------------------------------------------------------

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, v.nom, v.description, v.icone, '#4b5563', v.ordre, TRUE, CURRENT_TIMESTAMP
FROM categorie, (VALUES
    ('Services à domicile', 'Ménage, garde d''enfants, jardinage', 'home', 1),
    ('Services administratifs', 'Traduction, comptabilité, rédaction', 'file-text', 2),
    ('Services numériques', 'Web, design, community management', 'monitor', 3)
) AS v(nom, description, icone, ordre)
WHERE categorie.nom = 'Services'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Ménage', 'Nettoyage de la maison ou du bureau'),
    ('Jardinage', 'Entretien d''espaces verts'),
    ('Garde d''enfants', 'Baby-sitting et garde à domicile'),
    ('Cuisine à domicile', 'Préparation de repas chez le client')
) AS v(libelle, description)
WHERE c.nom = 'Services à domicile' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Traduction', 'Traduction de documents'),
    ('Rédaction de documents', 'Rédaction administrative ou professionnelle'),
    ('Comptabilité', 'Tenue de comptes et déclarations')
) AS v(libelle, description)
WHERE c.nom = 'Services administratifs' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Développement web', 'Création de sites et applications'),
    ('Community management', 'Gestion de réseaux sociaux'),
    ('Design graphique', 'Logos, affiches, supports visuels'),
    ('Photographie', 'Séances photo événementielles ou pro')
) AS v(libelle, description)
WHERE c.nom = 'Services numériques' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

-------------------------------------------------------
-- HÔTELLERIE
-------------------------------------------------------

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, 'Hébergement', 'Chambres et logements meublés', 'bed', '#0ea5e9', 1, TRUE, CURRENT_TIMESTAMP
FROM categorie WHERE categorie.nom = 'Hôtellerie'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'PRODUIT', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Chambre simple', 'Chambre pour une personne'),
    ('Chambre double', 'Chambre pour deux personnes'),
    ('Suite', 'Chambre haut de gamme'),
    ('Appartement meublé', 'Appartement complet meublé')
) AS v(libelle, description)
WHERE c.nom = 'Hébergement' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

-------------------------------------------------------
-- ADMINISTRATION
-------------------------------------------------------

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, 'Démarches administratives', 'Documents officiels et démarches', 'file-text', '#334155', 1, TRUE, CURRENT_TIMESTAMP
FROM categorie WHERE categorie.nom = 'Administration'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c CROSS JOIN (VALUES
    ('Légalisation de documents', 'Légalisation et authentification'),
    ('Passeport et état civil', 'Démarches d''état civil'),
    ('Permis de conduire', 'Démarches liées au permis'),
    ('Certification de documents', 'Copies certifiées conformes')
) AS v(libelle, description)
WHERE c.nom = 'Démarches administratives' AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);
