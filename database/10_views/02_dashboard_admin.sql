DROP VIEW IF EXISTS vue_dashboard_admin CASCADE;

CREATE VIEW vue_dashboard_admin AS
SELECT
    (SELECT COUNT(*) FROM utilisateurs) AS total_utilisateurs,
    (SELECT COUNT(*) FROM espace_professionnel) AS total_espaces,
    (SELECT COUNT(*) FROM offre) AS total_offres,
    (SELECT COUNT(*) FROM commande) AS total_commandes,
    (SELECT COUNT(*) FROM reservation) AS total_reservations,
    (SELECT COUNT(*) FROM litige WHERE statut='OUVERT') AS litiges_ouverts;