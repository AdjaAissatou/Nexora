-- Ajoute un attribut "Marque" (liste déroulante) aux catégories de produits
-- où la marque est un critère pertinent et énumérable — pour que la personne
-- choisisse plutôt que de taper. "Modèle" reste en texte libre : impossible
-- d'énumérer les centaines de références par marque sans explosion du
-- catalogue (contrairement à Taille/Couleur/Marque qui ont un nombre fini
-- de valeurs raisonnables).
-- Idempotent (comme 08/09/10).

INSERT INTO attribut (id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, ordre_affichage, actif, date_creation)
SELECT c.id_categorie, 'Marque', v.code, 'LISTE', FALSE, TRUE, TRUE, 0, TRUE, CURRENT_TIMESTAMP
FROM categorie c
JOIN (VALUES
    ('Ordinateurs', 'marque_ordinateur'),
    ('Périphériques informatiques', 'marque_peripherique'),
    ('Vêtements', 'marque_vetement'),
    ('Chaussures', 'marque_chaussure'),
    ('Accessoires de mode', 'marque_accessoire')
) AS v(categorie_nom, code) ON v.categorie_nom = c.nom
WHERE NOT EXISTS (SELECT 1 FROM attribut a WHERE a.code = v.code);

INSERT INTO valeur_attribut_possible (id_attribut, valeur, ordre_affichage, actif)
SELECT a.id_attribut, v.valeur, v.ordre, TRUE
FROM attribut a
CROSS JOIN (VALUES
    ('HP',1), ('Dell',2), ('Lenovo',3), ('Asus',4), ('Apple',5), ('Acer',6), ('Toshiba',7), ('Samsung',8), ('MSI',9), ('Autre',10)
) AS v(valeur, ordre)
WHERE a.code = 'marque_ordinateur'
  AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible p WHERE p.id_attribut = a.id_attribut AND p.valeur = v.valeur);

INSERT INTO valeur_attribut_possible (id_attribut, valeur, ordre_affichage, actif)
SELECT a.id_attribut, v.valeur, v.ordre, TRUE
FROM attribut a
CROSS JOIN (VALUES
    ('Logitech',1), ('HP',2), ('Canon',3), ('Epson',4), ('Brother',5), ('Redragon',6),
    ('Samsung',7), ('LG',8), ('Seagate',9), ('Western Digital',10), ('Asus',11), ('Dell',12), ('Autre',13)
) AS v(valeur, ordre)
WHERE a.code = 'marque_peripherique'
  AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible p WHERE p.id_attribut = a.id_attribut AND p.valeur = v.valeur);

INSERT INTO valeur_attribut_possible (id_attribut, valeur, ordre_affichage, actif)
SELECT a.id_attribut, v.valeur, v.ordre, TRUE
FROM attribut a
CROSS JOIN (VALUES
    ('Sans marque / fait sur mesure',1), ('Nike',2), ('Adidas',3), ('Zara',4), ('H&M',5), ('Puma',6), ('Autre',7)
) AS v(valeur, ordre)
WHERE a.code = 'marque_vetement'
  AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible p WHERE p.id_attribut = a.id_attribut AND p.valeur = v.valeur);

INSERT INTO valeur_attribut_possible (id_attribut, valeur, ordre_affichage, actif)
SELECT a.id_attribut, v.valeur, v.ordre, TRUE
FROM attribut a
CROSS JOIN (VALUES
    ('Sans marque',1), ('Nike',2), ('Adidas',3), ('Puma',4), ('Clarks',5), ('Autre',6)
) AS v(valeur, ordre)
WHERE a.code = 'marque_chaussure'
  AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible p WHERE p.id_attribut = a.id_attribut AND p.valeur = v.valeur);

INSERT INTO valeur_attribut_possible (id_attribut, valeur, ordre_affichage, actif)
SELECT a.id_attribut, v.valeur, v.ordre, TRUE
FROM attribut a
CROSS JOIN (VALUES
    ('Sans marque',1), ('Michael Kors',2), ('Fossil',3), ('Autre',4)
) AS v(valeur, ordre)
WHERE a.code = 'marque_accessoire'
  AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible p WHERE p.id_attribut = a.id_attribut AND p.valeur = v.valeur);
