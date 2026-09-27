DROP VIEW IF EXISTS vue_dashboard_utilisateur CASCADE;

CREATE VIEW vue_dashboard_utilisateur AS
SELECT
    u.id_utilisateur,
    u.nom,
    u.prenom,
    COUNT(DISTINCT p.id_panier) AS paniers,
    COUNT(DISTINCT c.id_commande) AS commandes,
    COUNT(DISTINCT f.id_favori) AS favoris,
    COUNT(DISTINCT conv.id_conversation) AS conversations
FROM utilisateurs u
LEFT JOIN panier p
       ON u.id_utilisateur = p.id_utilisateur
LEFT JOIN commande c
       ON u.id_utilisateur = c.id_utilisateur
LEFT JOIN favori f
       ON u.id_utilisateur = f.id_utilisateur
LEFT JOIN participant_conversation pc
       ON u.id_utilisateur = pc.id_utilisateur
LEFT JOIN conversation conv
       ON pc.id_conversation = conv.id_conversation
GROUP BY u.id_utilisateur, u.nom, u.prenom;