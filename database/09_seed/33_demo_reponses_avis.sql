-- ============================================================================
-- Démonstration (docs/architecture-acteurs.md §22) : quelques réponses de professionnels aux
-- avis de 25_demo_avis.sql, surtout aux critiques. Les avis de Boutique Sarah Mode restent sans
-- réponse, pour essayer l'onglet « Avis reçus ». Avis retrouvés par espace et début du
-- commentaire (les identifiants diffèrent d'une base à l'autre). Rejouable : une réponse déjà
-- écrite n'est jamais remplacée.
-- ============================================================================

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

UPDATE avis a SET reponse_fournisseur = r.reponse, date_reponse = a.date_creation + INTERVAL '1 day'
FROM espace_professionnel e,
     (VALUES
        ('Auchan Discount Sénégal', 'Pratique, mais certains produits manquent',
         'Merci pour votre retour. Nous avons revu nos commandes : les ruptures sur l''huile et le riz sont désormais rares. N''hésitez pas à demander à l''accueil.'),
        ('TechnoPlus Informatique', 'Bons produits, mais le service après-vente',
         'Désolés pour l''attente. Le SAV répond maintenant aussi sur WhatsApp, du lundi au samedi : écrivez-nous, nous suivons votre dossier.'),
        ('Plomberie Ndiaye & Fils', 'Travail correct mais arrivés avec une heure de retard',
         'Toutes nos excuses pour ce retard dû aux embouteillages. Nous vous prévenons désormais par SMS si nous avons du retard.'),
        ('Plomberie Ndiaye & Fils', 'Débouchage réglé en vingt minutes',
         'Merci beaucoup ! Au plaisir de vous dépanner à nouveau.'),
        ('Atelier Diagne Bois', 'Beau travail sur nos portes',
         'Merci ! Le délai venait du séchage du bois, indispensable pour éviter qu''il ne travaille. Nous l''indiquons maintenant dès le devis.'),
        ('Baye Transport', 'Colis livré à Thiès le jour même',
         'Merci pour votre confiance, à bientôt pour vos prochains envois !')
     ) AS r(espace, debut, reponse)
WHERE e.id_espace = a.id_espace AND e.nom = r.espace AND a.commentaire LIKE r.debut || '%'
  AND a.reponse_fournisseur IS NULL;
