-- Complète la catégorisation hiérarchique pour Informatique : jusqu'ici un seul
-- type d'offre générique ("Produits informatiques") regroupait ordinateurs,
-- imprimantes, souris, claviers, écrans, disques durs... — le professionnel
-- devait taper un titre depuis rien, sans aucune proposition de produit précis.
-- Idempotent (comme 08_sous_categories.sql).

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, 'Ordinateurs', 'Ordinateurs portables et de bureau', 'laptop', '#2563eb', 1, TRUE, CURRENT_TIMESTAMP
FROM categorie WHERE nom = 'Informatique'
ON CONFLICT ((COALESCE(id_categorie_parent, 0)), nom) DO NOTHING;

INSERT INTO categorie (id_categorie_parent, nom, description, icone, couleur, ordre_affichage, actif, date_creation)
SELECT id_categorie, 'Périphériques informatiques', 'Imprimantes, souris, claviers, écrans, stockage', 'mouse', '#2563eb', 2, TRUE, CURRENT_TIMESTAMP
FROM categorie WHERE nom = 'Informatique'
ON CONFLICT ((COALESCE(id_categorie_parent, 0)), nom) DO NOTHING;

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'PRODUIT', TRUE
FROM categorie c
CROSS JOIN (VALUES
    ('Ordinateur portable', 'PC portable, toutes marques'),
    ('PC de bureau', 'Unité centrale de bureau, toutes marques')
) AS v(libelle, description)
WHERE c.nom = 'Ordinateurs'
  AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, v.libelle, v.description, 'PRODUIT', TRUE
FROM categorie c
CROSS JOIN (VALUES
    ('Imprimante', 'Imprimante jet d''encre ou laser'),
    ('Souris', 'Souris filaire ou sans fil'),
    ('Clavier', 'Clavier classique ou mécanique'),
    ('Écran', 'Moniteur / écran d''ordinateur'),
    ('Disque dur externe', 'Stockage externe USB')
) AS v(libelle, description)
WHERE c.nom = 'Périphériques informatiques'
  AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie AND t.libelle = v.libelle);

-- Réaligne les offres de démo déjà créées sur les nouveaux types précis
-- (elles pointaient vers le type générique "Produits informatiques").
UPDATE offre o SET
    id_categorie = c.id_categorie,
    id_type_offre = t.id_type_offre
FROM categorie c
JOIN type_offre t ON t.id_categorie = c.id_categorie
WHERE (o.titre, t.libelle) IN (
    ('Ordinateur portable HP Pavilion 15', 'Ordinateur portable'),
    ('PC de bureau Dell OptiPlex 3090', 'PC de bureau'),
    ('Imprimante Canon Pixma G2010', 'Imprimante'),
    ('Souris sans fil Logitech M185', 'Souris'),
    ('Clavier mécanique gaming Redragon K552', 'Clavier'),
    ('Écran LED 24 pouces Samsung S24F350', 'Écran'),
    ('Disque dur externe Seagate 1To', 'Disque dur externe')
);
