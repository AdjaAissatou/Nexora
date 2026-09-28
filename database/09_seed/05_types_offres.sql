INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, 'Produits informatiques', 'Ordinateurs, téléphones, accessoires et matériel informatique', 'PRODUIT', TRUE
FROM categorie
WHERE nom = 'Informatique';

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, 'Consultations médicales', 'Consultations, soins et services de santé', 'SERVICE', TRUE
FROM categorie
WHERE nom = 'Santé';

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, 'Repas et boissons', 'Plats, menus, boissons et restauration', 'PRODUIT', TRUE
FROM categorie
WHERE nom = 'Restauration';

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, 'Produits de vente', 'Vente de produits divers en boutique', 'PRODUIT', TRUE
FROM categorie
WHERE nom = 'Commerce';

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, 'Prestations beauté', 'Coiffure, esthétique, soins et beauté', 'SERVICE', TRUE
FROM categorie
WHERE nom = 'Beauté';

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, 'Transport et livraison', 'Taxis, VTC, livraison et déplacement', 'SERVICE', TRUE
FROM categorie
WHERE nom = 'Transport';

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, 'Biens immobiliers', 'Maisons, appartements, terrains et locations', 'PRODUIT', TRUE
FROM categorie
WHERE nom = 'Immobilier';

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, 'Formations et cours', 'Écoles, universités, cours et formations', 'SERVICE', TRUE
FROM categorie
WHERE nom = 'Éducation';

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, 'Services professionnels', 'Services divers proposés par des professionnels', 'SERVICE', TRUE
FROM categorie
WHERE nom = 'Services';

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, 'Véhicules et accessoires', 'Véhicules, pièces et services automobiles', 'PRODUIT', TRUE
FROM categorie
WHERE nom = 'Automobile';

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, 'Hébergement', 'Hôtels, chambres et services d’hébergement', 'SERVICE', TRUE
FROM categorie
WHERE nom = 'Hôtellerie';

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, 'Services administratifs', 'Services et démarches administratives', 'SERVICE', TRUE
FROM categorie
WHERE nom = 'Administration';
