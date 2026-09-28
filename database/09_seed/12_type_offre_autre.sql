-- Ajoute un type d'offre "Autre" à chaque catégorie feuille qui a déjà des
-- types précis — pour que la personne ne soit jamais bloquée si son produit
-- ou service exact n'est pas dans la liste proposée : elle choisit "Autre"
-- et décrit librement dans le titre.
-- Reprend automatiquement le PRODUIT/SERVICE dominant de la catégorie.
-- Idempotent.

INSERT INTO type_offre (id_categorie, libelle, description, principale, actif)
SELECT c.id_categorie, 'Autre', 'Produit ou service non listé — à préciser dans le titre',
       (SELECT t.principale FROM type_offre t WHERE t.id_categorie = c.id_categorie LIMIT 1),
       TRUE
FROM categorie c
WHERE EXISTS (SELECT 1 FROM type_offre t2 WHERE t2.id_categorie = c.id_categorie)
  AND NOT EXISTS (SELECT 1 FROM type_offre t3 WHERE t3.id_categorie = c.id_categorie AND t3.libelle = 'Autre');
