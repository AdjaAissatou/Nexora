-- Rattachement type_espace -> domaines racines pertinents (voir 15_categorie_type_espace.sql).
-- Chaque type d'espace a un ou plusieurs domaines "principaux" (affichés en tête de liste
-- dans "créer une offre") et éventuellement des domaines secondaires plausibles.
-- Idempotent.

INSERT INTO categorie_type_espace (id_categorie, id_type_espace, principal)
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Commerce' AND c.id_categorie_parent IS NULL AND t.nom = 'Boutique'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Informatique' AND c.id_categorie_parent IS NULL AND t.nom = 'Boutique'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Mode et textile' AND c.id_categorie_parent IS NULL AND t.nom = 'Boutique'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Électronique et électroménager' AND c.id_categorie_parent IS NULL AND t.nom = 'Boutique'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Maison et habitat' AND c.id_categorie_parent IS NULL AND t.nom = 'Boutique'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Beauté' AND c.id_categorie_parent IS NULL AND t.nom = 'Boutique'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Agroalimentaire' AND c.id_categorie_parent IS NULL AND t.nom = 'Boutique'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Agriculture et élevage' AND c.id_categorie_parent IS NULL AND t.nom = 'Boutique'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Pêche et aquaculture' AND c.id_categorie_parent IS NULL AND t.nom = 'Boutique'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Animaux' AND c.id_categorie_parent IS NULL AND t.nom = 'Boutique'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Bâtiment et construction' AND c.id_categorie_parent IS NULL AND t.nom = 'Boutique'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Services' AND c.id_categorie_parent IS NULL AND t.nom = 'Service'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Informatique' AND c.id_categorie_parent IS NULL AND t.nom = 'Service'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Conseil et services aux entreprises' AND c.id_categorie_parent IS NULL AND t.nom = 'Service'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Services à la personne' AND c.id_categorie_parent IS NULL AND t.nom = 'Service'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Artisanat et réparation' AND c.id_categorie_parent IS NULL AND t.nom = 'Service'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Communication et médias' AND c.id_categorie_parent IS NULL AND t.nom = 'Service'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Sécurité et sûreté' AND c.id_categorie_parent IS NULL AND t.nom = 'Service'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Événementiel' AND c.id_categorie_parent IS NULL AND t.nom = 'Service'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Juridique et administratif' AND c.id_categorie_parent IS NULL AND t.nom = 'Service'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Finance et assurance' AND c.id_categorie_parent IS NULL AND t.nom = 'Service'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Énergie et environnement' AND c.id_categorie_parent IS NULL AND t.nom = 'Service'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Transport' AND c.id_categorie_parent IS NULL AND t.nom = 'Service'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Restauration' AND c.id_categorie_parent IS NULL AND t.nom = 'Restaurant'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Agroalimentaire' AND c.id_categorie_parent IS NULL AND t.nom = 'Restaurant'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Événementiel' AND c.id_categorie_parent IS NULL AND t.nom = 'Restaurant'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Hôtellerie' AND c.id_categorie_parent IS NULL AND t.nom = 'Hôtel'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Tourisme et voyage' AND c.id_categorie_parent IS NULL AND t.nom = 'Hôtel'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Restauration' AND c.id_categorie_parent IS NULL AND t.nom = 'Hôtel'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Événementiel' AND c.id_categorie_parent IS NULL AND t.nom = 'Hôtel'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Santé' AND c.id_categorie_parent IS NULL AND t.nom = 'Clinique'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Santé' AND c.id_categorie_parent IS NULL AND t.nom = 'Pharmacie'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Automobile' AND c.id_categorie_parent IS NULL AND t.nom = 'Garage'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Éducation' AND c.id_categorie_parent IS NULL AND t.nom = 'École'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Éducation' AND c.id_categorie_parent IS NULL AND t.nom = 'Université'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Commerce' AND c.id_categorie_parent IS NULL AND t.nom = 'Supermarché'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Agroalimentaire' AND c.id_categorie_parent IS NULL AND t.nom = 'Supermarché'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Maison et habitat' AND c.id_categorie_parent IS NULL AND t.nom = 'Supermarché'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Électronique et électroménager' AND c.id_categorie_parent IS NULL AND t.nom = 'Supermarché'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Beauté' AND c.id_categorie_parent IS NULL AND t.nom = 'Supermarché'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Mode et textile' AND c.id_categorie_parent IS NULL AND t.nom = 'Supermarché'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Juridique et administratif' AND c.id_categorie_parent IS NULL AND t.nom = 'Cabinet'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Conseil et services aux entreprises' AND c.id_categorie_parent IS NULL AND t.nom = 'Cabinet'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Finance et assurance' AND c.id_categorie_parent IS NULL AND t.nom = 'Cabinet'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Santé' AND c.id_categorie_parent IS NULL AND t.nom = 'Cabinet'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Immobilier' AND c.id_categorie_parent IS NULL AND t.nom = 'Cabinet'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Immobilier' AND c.id_categorie_parent IS NULL AND t.nom = 'Agence'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Tourisme et voyage' AND c.id_categorie_parent IS NULL AND t.nom = 'Agence'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Communication et médias' AND c.id_categorie_parent IS NULL AND t.nom = 'Agence'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Conseil et services aux entreprises' AND c.id_categorie_parent IS NULL AND t.nom = 'Agence'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Transport' AND c.id_categorie_parent IS NULL AND t.nom = 'Agence'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Artisanat et réparation' AND c.id_categorie_parent IS NULL AND t.nom = 'Atelier'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Informatique' AND c.id_categorie_parent IS NULL AND t.nom = 'Atelier'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Mode et textile' AND c.id_categorie_parent IS NULL AND t.nom = 'Atelier'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Automobile' AND c.id_categorie_parent IS NULL AND t.nom = 'Atelier'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Bâtiment et construction' AND c.id_categorie_parent IS NULL AND t.nom = 'Atelier'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Industrie et fabrication' AND c.id_categorie_parent IS NULL AND t.nom = 'Atelier'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Associations et action sociale' AND c.id_categorie_parent IS NULL AND t.nom = 'Association'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Culture loisirs et sport' AND c.id_categorie_parent IS NULL AND t.nom = 'Association'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, FALSE FROM categorie c, type_espace t WHERE c.nom = 'Éducation' AND c.id_categorie_parent IS NULL AND t.nom = 'Association'
UNION ALL
SELECT c.id_categorie, t.id_type_espace, TRUE FROM categorie c, type_espace t WHERE c.nom = 'Administration' AND c.id_categorie_parent IS NULL AND t.nom = 'Administration'
ON CONFLICT (id_categorie, id_type_espace) DO NOTHING;
