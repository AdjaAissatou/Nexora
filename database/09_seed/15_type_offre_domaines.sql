-- Les catégories-feuilles ajoutées par 14_taxonomie_domaines.sql (ex: "Riz", "Robe
-- bazin", "Vidange") sont déjà le produit/service concret : on leur donne un type_offre
-- portant leur propre nom, pour que "Que proposez-vous ?" ne soit jamais vide en bout
-- de cascade. PRODUIT/SERVICE par défaut selon le domaine racine (même logique que les
-- domaines déjà détaillés : Commerce/Informatique/Restauration = PRODUIT majoritaire,
-- Services/Santé/Éducation = SERVICE majoritaire). Idempotent.

WITH RECURSIVE racine AS (
    SELECT id_categorie, id_categorie AS id_racine, nom AS nom_racine
    FROM categorie WHERE id_categorie_parent IS NULL
    UNION ALL
    SELECT c.id_categorie, r.id_racine, r.nom_racine
    FROM categorie c JOIN racine r ON c.id_categorie_parent = r.id_categorie
),
defaut_domaine(nom_racine, principale) AS (
    VALUES
        ('Administration', 'SERVICE'), ('Automobile', 'SERVICE'), ('Beauté', 'SERVICE'),
        ('Commerce', 'PRODUIT'), ('Hôtellerie', 'PRODUIT'), ('Immobilier', 'PRODUIT'),
        ('Informatique', 'PRODUIT'), ('Restauration', 'PRODUIT'), ('Santé', 'SERVICE'),
        ('Services', 'SERVICE'), ('Transport', 'SERVICE'), ('Éducation', 'SERVICE'),
        ('Agriculture et élevage', 'PRODUIT'), ('Agroalimentaire', 'PRODUIT'), ('Animaux', 'PRODUIT'),
        ('Artisanat et réparation', 'SERVICE'), ('Associations et action sociale', 'SERVICE'),
        ('Bâtiment et construction', 'PRODUIT'), ('Communication et médias', 'SERVICE'),
        ('Conseil et services aux entreprises', 'SERVICE'), ('Culture loisirs et sport', 'SERVICE'),
        ('Finance et assurance', 'SERVICE'), ('Industrie et fabrication', 'PRODUIT'),
        ('Juridique et administratif', 'SERVICE'), ('Maison et habitat', 'PRODUIT'),
        ('Mode et textile', 'PRODUIT'), ('Pêche et aquaculture', 'PRODUIT'),
        ('Services à la personne', 'SERVICE'), ('Sécurité et sûreté', 'SERVICE'),
        ('Tourisme et voyage', 'SERVICE'), ('Électronique et électroménager', 'PRODUIT'),
        ('Énergie et environnement', 'SERVICE'), ('Événementiel', 'SERVICE')
),
feuilles AS (
    SELECT c.id_categorie, c.nom, COALESCE(d.principale, 'PRODUIT') AS principale
    FROM categorie c
    JOIN racine r ON r.id_categorie = c.id_categorie
    LEFT JOIN defaut_domaine d ON d.nom_racine = r.nom_racine
    WHERE c.actif = TRUE
      AND NOT EXISTS (SELECT 1 FROM categorie e WHERE e.id_categorie_parent = c.id_categorie AND e.actif = TRUE)
      AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = c.id_categorie)
)
INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT id_categorie, nom, NULL, principale::type_offre_principale, TRUE
FROM feuilles;
