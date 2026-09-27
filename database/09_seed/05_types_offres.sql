INSERT INTO type_offre (id_categorie, libelle, description, actif)
SELECT id_categorie, 'Produits informatiques', 'Ordinateurs, téléphones, accessoires et matériel informatique', TRUE
FROM categorie
WHERE nom = 'Informatique';

INSERT INTO type_offre (id_categorie, libelle, description, actif)
SELECT id_categorie, 'Consultations médicales', 'Consultations, soins et services de santé', TRUE
FROM categorie
WHERE nom = 'Santé';

INSERT INTO type_offre (id_categorie, libelle, description, actif)
SELECT id_categorie, 'Repas et boissons', 'Plats, menus, boissons et restauration', TRUE
FROM categorie
WHERE nom = 'Restauration';

INSERT INTO type_offre (id_categorie, libelle, description, actif)
SELECT id_categorie, 'Produits de vente', 'Vente de produits divers en boutique', TRUE
FROM categorie
WHERE nom = 'Commerce';

INSERT INTO type_offre (id_categorie, libelle, description, actif)
SELECT id_categorie, 'Prestations beauté', 'Coiffure, esthétique, soins et beauté', TRUE
FROM categorie
WHERE nom = 'Beauté';

INSERT INTO type_offre (id_categorie, libelle, description, actif)
SELECT id_categorie, 'Transport et livraison', 'Taxis, VTC, livraison et déplacement', TRUE
FROM categorie
WHERE nom = 'Transport';

INSERT INTO type_offre (id_categorie, libelle, description, actif)
SELECT id_categorie, 'Biens immobiliers', 'Maisons, appartements, terrains et locations', TRUE
FROM categorie
WHERE nom = 'Immobilier';

INSERT INTO type_offre (id_categorie, libelle, description, actif)
SELECT id_categorie, 'Formations et cours', 'Écoles, universités, cours et formations', TRUE
FROM categorie
WHERE nom = 'Éducation';

INSERT INTO type_offre (id_categorie, libelle, description, actif)
SELECT id_categorie, 'Services professionnels', 'Services divers proposés par des professionnels', TRUE
FROM categorie
WHERE nom = 'Services';

INSERT INTO type_offre (id_categorie, libelle, description, actif)
SELECT id_categorie, 'Véhicules et accessoires', 'Véhicules, pièces et services automobiles', TRUE
FROM categorie
WHERE nom = 'Automobile';

INSERT INTO type_offre (id_categorie, libelle, description, actif)
SELECT id_categorie, 'Hébergement', 'Hôtels, chambres et services d’hébergement', TRUE
FROM categorie
WHERE nom = 'Hôtellerie';

INSERT INTO type_offre (id_categorie, libelle, description, actif)
SELECT id_categorie, 'Services administratifs', 'Services et démarches administratives', TRUE
FROM categorie
WHERE nom = 'Administration';