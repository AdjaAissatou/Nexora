DROP VIEW IF EXISTS vue_statistiques CASCADE;

CREATE VIEW vue_statistiques AS
SELECT
    (SELECT COUNT(*) FROM utilisateurs) AS utilisateurs,
    (SELECT COUNT(*) FROM espace_professionnel) AS espaces,
    (SELECT COUNT(*) FROM offre) AS offres,
    (SELECT COUNT(*) FROM produit) AS produits,
    (SELECT COUNT(*) FROM service) AS services,
    (SELECT COUNT(*) FROM commande) AS commandes,
    (SELECT COUNT(*) FROM reservation) AS reservations,
    (SELECT COUNT(*) FROM avis) AS avis,
    (SELECT COUNT(*) FROM litige) AS litiges;