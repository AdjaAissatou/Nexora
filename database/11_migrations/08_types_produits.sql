-- ============================================================================
-- 07/10 : produits vendables rangés comme services.
--
-- Des rayons « de service » contiennent aussi des produits : une pharmacie vend des médicaments,
-- un garage des pneus et des batteries, un artisan sa poterie. Ces types d'offre avaient été
-- enregistrés comme SERVICE : le formulaire leur proposait durée et intervention à domicile au
-- lieu de l'état, du stock et de la garantie, et ils n'apparaissaient pas dans « Boutique ».
-- Réparation, location, assurance et formation (« Réparation > Téléphones », « Location de
-- matériel > Tentes »…) restent des services. Chaque type est désigné par son rayon et son
-- libellé (les identifiants diffèrent d'une base à l'autre). « Autre » suit les autres types
-- de son rayon. Rejouable.
-- ============================================================================

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

UPDATE type_offre t SET principale = 'PRODUIT'
FROM categorie c
WHERE c.id_categorie = t.id_categorie
  AND CAST(t.principale AS TEXT) = 'SERVICE'
  AND (c.nom, t.libelle) IN (
      ('Médicaments', 'Médicaments'),
      ('Parapharmacie', 'Parapharmacie'),
      ('Hygiène et soins', 'Hygiène et soins'),
      ('Mobilité', 'Mobilité'),
      ('Parfums', 'Parfums'),
      ('Batteries', 'Batteries'),
      ('Pneus', 'Pneus'),
      ('Pièces moteur', 'Pièces moteur'),
      ('Motos', 'Motos'),
      ('Voitures d''occasion', 'Voitures d''occasion'),
      ('Voitures neuves', 'Voitures neuves'),
      ('Objets traditionnels', 'Objets traditionnels'),
      ('Bijouterie artisanale', 'Bijouterie artisanale'),
      ('Poterie', 'Poterie'),
      ('Paniererie', 'Paniererie'));

UPDATE type_offre a SET principale = 'PRODUIT'
WHERE a.libelle ~* '^autre' AND CAST(a.principale AS TEXT) = 'SERVICE'
  AND EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = a.id_categorie AND t.libelle !~* '^autre')
  AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = a.id_categorie AND t.libelle !~* '^autre'
                  AND CAST(t.principale AS TEXT) = 'SERVICE');
