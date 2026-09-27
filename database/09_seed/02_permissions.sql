INSERT INTO permissions (code, nom, description, module, actif, date_creation)
VALUES
('GERER_UTILISATEURS', 'Gérer les utilisateurs', 'Créer, modifier, supprimer et consulter les utilisateurs', 'SECURITY', TRUE, CURRENT_TIMESTAMP),
('GERER_ROLES', 'Gérer les rôles', 'Créer, modifier et affecter les rôles', 'SECURITY', TRUE, CURRENT_TIMESTAMP),
('GERER_PERMISSIONS', 'Gérer les permissions', 'Créer et configurer les permissions', 'SECURITY', TRUE, CURRENT_TIMESTAMP),

('GERER_ESPACES', 'Gérer les espaces professionnels', 'Créer, modifier, valider et suspendre les espaces', 'PROFESSIONAL', TRUE, CURRENT_TIMESTAMP),
('GERER_CERTIFICATIONS', 'Gérer les certifications', 'Traiter les demandes de certification', 'PROFESSIONAL', TRUE, CURRENT_TIMESTAMP),
('GERER_HORAIRES', 'Gérer les horaires', 'Configurer les horaires des espaces', 'PROFESSIONAL', TRUE, CURRENT_TIMESTAMP),

('GERER_CATEGORIES', 'Gérer les catégories', 'Créer, modifier et supprimer les catégories', 'CATALOGUE', TRUE, CURRENT_TIMESTAMP),
('GERER_TYPES_OFFRES', 'Gérer les types d’offres', 'Configurer les types d’offres', 'CATALOGUE', TRUE, CURRENT_TIMESTAMP),
('GERER_OFFRES', 'Gérer les offres', 'Créer, modifier, publier et supprimer les offres', 'CATALOGUE', TRUE, CURRENT_TIMESTAMP),
('GERER_ATTRIBUTS', 'Gérer les attributs', 'Configurer les attributs dynamiques', 'CATALOGUE', TRUE, CURRENT_TIMESTAMP),
('GERER_PROMOTIONS', 'Gérer les promotions', 'Créer et gérer les promotions', 'CATALOGUE', TRUE, CURRENT_TIMESTAMP),
('GERER_IMAGES', 'Gérer les images', 'Ajouter et supprimer les images des offres', 'CATALOGUE', TRUE, CURRENT_TIMESTAMP),

('CONSULTER_RECHERCHE', 'Consulter la recherche', 'Utiliser la recherche et les filtres', 'SEARCH', TRUE, CURRENT_TIMESTAMP),
('GERER_FAVORIS', 'Gérer les favoris', 'Ajouter ou retirer des favoris', 'SEARCH', TRUE, CURRENT_TIMESTAMP),
('GERER_AVIS', 'Gérer les avis', 'Créer, modifier et modérer les avis', 'SEARCH', TRUE, CURRENT_TIMESTAMP),
('GERER_SIGNALEMENTS', 'Gérer les signalements', 'Traiter les signalements', 'SEARCH', TRUE, CURRENT_TIMESTAMP),

('GERER_CONVERSATIONS', 'Gérer les conversations', 'Accéder aux discussions', 'COMMUNICATION', TRUE, CURRENT_TIMESTAMP),
('GERER_MESSAGES', 'Gérer les messages', 'Envoyer, lire et supprimer des messages', 'COMMUNICATION', TRUE, CURRENT_TIMESTAMP),
('GERER_NOTIFICATIONS', 'Gérer les notifications', 'Consulter et gérer les notifications', 'COMMUNICATION', TRUE, CURRENT_TIMESTAMP),

('GERER_PANIERS', 'Gérer les paniers', 'Ajouter et retirer des éléments du panier', 'COMMERCE', TRUE, CURRENT_TIMESTAMP),
('GERER_COMMANDES', 'Gérer les commandes', 'Créer et suivre les commandes', 'COMMERCE', TRUE, CURRENT_TIMESTAMP),
('GERER_SOUS_COMMANDES', 'Gérer les sous-commandes', 'Gérer les sous-commandes par fournisseur', 'COMMERCE', TRUE, CURRENT_TIMESTAMP),
('GERER_RESERVATIONS', 'Gérer les réservations', 'Créer et suivre les réservations', 'COMMERCE', TRUE, CURRENT_TIMESTAMP),
('GERER_LIVRAISONS', 'Gérer les livraisons', 'Suivre les livraisons', 'COMMERCE', TRUE, CURRENT_TIMESTAMP),
('GERER_PAIEMENTS', 'Gérer les paiements', 'Enregistrer et valider les paiements', 'COMMERCE', TRUE, CURRENT_TIMESTAMP),
('GERER_WALLET', 'Gérer le wallet', 'Consulter le solde et les transactions', 'COMMERCE', TRUE, CURRENT_TIMESTAMP),
('GERER_ESCROW', 'Gérer l’escrow', 'Bloquer et libérer les fonds', 'COMMERCE', TRUE, CURRENT_TIMESTAMP),
('GERER_LITIGES', 'Gérer les litiges', 'Traiter les litiges', 'COMMERCE', TRUE, CURRENT_TIMESTAMP),
('GERER_FACTURES', 'Gérer les factures', 'Émettre et consulter les factures', 'COMMERCE', TRUE, CURRENT_TIMESTAMP),

('VOIR_STATISTIQUES', 'Voir les statistiques', 'Consulter les indicateurs de la plateforme', 'ADMINISTRATION', TRUE, CURRENT_TIMESTAMP),
('GERER_PARAMETRES', 'Gérer les paramètres', 'Configurer les paramètres globaux', 'ADMINISTRATION', TRUE, CURRENT_TIMESTAMP),
('GERER_JOURNAL', 'Gérer le journal d’actions', 'Consulter les traces d’actions', 'ADMINISTRATION', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;