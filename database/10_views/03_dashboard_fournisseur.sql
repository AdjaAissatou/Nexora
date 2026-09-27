DROP VIEW IF EXISTS vue_dashboard_fournisseur CASCADE;

CREATE VIEW vue_dashboard_fournisseur AS
SELECT
    ep.id_espace,
    ep.nom,
    COUNT(DISTINCT o.id_offre) AS nombre_offres,
    COUNT(DISTINCT c.id_commande) AS nombre_commandes,
    COUNT(DISTINCT r.id_reservation) AS nombre_reservations
FROM espace_professionnel ep
LEFT JOIN offre o
       ON ep.id_espace = o.id_espace
LEFT JOIN sous_commande sc
       ON ep.id_espace = sc.id_espace
LEFT JOIN commande c
       ON sc.id_commande = c.id_commande
LEFT JOIN reservation r
       ON ep.id_espace = r.id_espace
GROUP BY ep.id_espace, ep.nom;