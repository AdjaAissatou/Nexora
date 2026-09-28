-- Fondations du catalogue hiérarchique : sous-catégories, types d'offre granulaires
-- et attributs (listes déroulantes) pour 3 métiers représentatifs, sur le modèle :
--   Automobile (Garage) -> service : plusieurs prestations
--   Commerce  (Boutique mode) -> produit : plusieurs catégories de produits avec attributs
--   Santé     (Cabinet dentaire) -> service : plusieurs prestations
-- Idempotent : rejouable sans dupliquer (categorie.nom est UNIQUE, le reste est
-- protégé par des WHERE NOT EXISTS).

-------------------------------------------------------
-- SOUS-CATÉGORIES
-------------------------------------------------------

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, 'Mécanique automobile', 'Entretien et réparation de véhicules', 'wrench', '#ea580c', 1, TRUE, CURRENT_TIMESTAMP
FROM categorie WHERE nom = 'Automobile'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, 'Mode', 'Vêtements, chaussures et accessoires', 'shirt', '#16a34a', 1, TRUE, CURRENT_TIMESTAMP
FROM categorie WHERE nom = 'Commerce'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, 'Vêtements', 'Robes, boubous, pantalons, chemises...', 'shirt', '#16a34a', 1, TRUE, CURRENT_TIMESTAMP
FROM categorie WHERE nom = 'Mode'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, 'Chaussures', 'Sandales, baskets, escarpins...', 'footprints', '#16a34a', 2, TRUE, CURRENT_TIMESTAMP
FROM categorie WHERE nom = 'Mode'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, 'Accessoires de mode', 'Sacs, bijoux, ceintures...', 'gem', '#16a34a', 3, TRUE, CURRENT_TIMESTAMP
FROM categorie WHERE nom = 'Mode'
ON CONFLICT (nom) DO NOTHING;

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, 'Soins dentaires', 'Consultations et prestations dentaires', 'stethoscope', '#dc2626', 1, TRUE, CURRENT_TIMESTAMP
FROM categorie WHERE nom = 'Santé'
ON CONFLICT (nom) DO NOTHING;

-------------------------------------------------------
-- TYPES D'OFFRE GRANULAIRES (les prestations/produits concrets d'un métier)
-------------------------------------------------------

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c
CROSS JOIN (VALUES
    ('Vidange', 'Vidange moteur et remplacement de filtres'),
    ('Diagnostic', 'Diagnostic électronique et mécanique'),
    ('Réparation moteur', 'Réparation et révision moteur'),
    ('Freinage', 'Entretien et réparation du système de freinage'),
    ('Climatisation', 'Entretien et recharge de climatisation')
) AS v(libelle, description)
WHERE c.nom = 'Mécanique automobile'
  AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, 'Vente de vêtements', 'Robes, boubous, pantalons, chemises...', 'PRODUIT', TRUE
FROM categorie c
WHERE c.nom = 'Vêtements'
  AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = 'Vente de vêtements');

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, 'Vente de chaussures', 'Sandales, baskets, escarpins...', 'PRODUIT', TRUE
FROM categorie c
WHERE c.nom = 'Chaussures'
  AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = 'Vente de chaussures');

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, 'Vente d''accessoires', 'Sacs, bijoux, ceintures...', 'PRODUIT', TRUE
FROM categorie c
WHERE c.nom = 'Accessoires de mode'
  AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = 'Vente d''accessoires');

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'SERVICE', TRUE
FROM categorie c
CROSS JOIN (VALUES
    ('Consultation dentaire', 'Consultation et examen dentaire'),
    ('Détartrage', 'Détartrage et nettoyage dentaire'),
    ('Extraction dentaire', 'Extraction de dent'),
    ('Prothèse dentaire', 'Pose de prothèse dentaire'),
    ('Blanchiment dentaire', 'Blanchiment des dents')
) AS v(libelle, description)
WHERE c.nom = 'Soins dentaires'
  AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

-------------------------------------------------------
-- ATTRIBUTS (listes déroulantes par catégorie — pas de saisie libre)
-------------------------------------------------------

INSERT INTO attribut (id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, ordre_affichage, actif, date_creation)
SELECT c.id_categorie, 'Taille', 'vetement_taille', 'LISTE', TRUE, TRUE, TRUE, 1, TRUE, CURRENT_TIMESTAMP
FROM categorie c
WHERE c.nom = 'Vêtements'
  AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.code = 'vetement_taille');

INSERT INTO attribut (id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, ordre_affichage, actif, date_creation)
SELECT c.id_categorie, 'Couleur', 'vetement_couleur', 'LISTE', FALSE, TRUE, TRUE, 2, TRUE, CURRENT_TIMESTAMP
FROM categorie c
WHERE c.nom = 'Vêtements'
  AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.code = 'vetement_couleur');

INSERT INTO attribut (id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, ordre_affichage, actif, date_creation)
SELECT c.id_categorie, 'Pointure', 'chaussure_pointure', 'LISTE', TRUE, TRUE, TRUE, 1, TRUE, CURRENT_TIMESTAMP
FROM categorie c
WHERE c.nom = 'Chaussures'
  AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.code = 'chaussure_pointure');

INSERT INTO valeur_attribut_possible (id_attribut, valeur, ordre_affichage, actif)
SELECT a.id_attribut, v.valeur, v.ordre, TRUE
FROM attribut a
CROSS JOIN (VALUES ('S',1), ('M',2), ('L',3), ('XL',4), ('XXL',5)) AS v(valeur, ordre)
WHERE a.code = 'vetement_taille'
  AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible p WHERE p.id_attribut = a.id_attribut AND p.valeur = v.valeur);

INSERT INTO valeur_attribut_possible (id_attribut, valeur, ordre_affichage, actif)
SELECT a.id_attribut, v.valeur, v.ordre, TRUE
FROM attribut a
CROSS JOIN (VALUES ('Rouge',1), ('Bleu',2), ('Vert',3), ('Noir',4), ('Blanc',5), ('Jaune',6)) AS v(valeur, ordre)
WHERE a.code = 'vetement_couleur'
  AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible p WHERE p.id_attribut = a.id_attribut AND p.valeur = v.valeur);

INSERT INTO valeur_attribut_possible (id_attribut, valeur, ordre_affichage, actif)
SELECT a.id_attribut, v.valeur::text, v.ordre, TRUE
FROM attribut a
CROSS JOIN (VALUES (36,1), (37,2), (38,3), (39,4), (40,5), (41,6), (42,7), (43,8), (44,9), (45,10)) AS v(valeur, ordre)
WHERE a.code = 'chaussure_pointure'
  AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible p WHERE p.id_attribut = a.id_attribut AND p.valeur = v.valeur::text);
