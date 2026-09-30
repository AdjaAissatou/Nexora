-- ============================================================================
-- Rôles et permissions (docs/architecture-acteurs.md §6 et §9.2).
--
-- Modèle : utilisateur → compte (accounts) → rôles (account_roles) → permissions
-- (role_permissions). Le jeton JWT porte les rôles ET les permissions effectives
-- du compte (union des permissions de ses rôles actifs) ; les services vérifient
-- une PERMISSION (hasAuthority('PERM_...')), jamais une liste de rôles écrite en dur.
-- Modifier les permissions d'un rôle change donc les droits sans toucher au code.
--
-- Script rejouable : complète le catalogue, précise les descriptions, pose la
-- matrice par défaut. Le rejouer rétablit les liaisons par défaut retirées depuis le
-- back-office, sans toucher à celles qui y ont été ajoutées.
-- ============================================================================

-- 1. Permissions du back-office qui manquaient au catalogue.
INSERT INTO permissions (code, name, description, module, active, created_at)
VALUES
('ACCEDER_BACK_OFFICE', 'Accéder au back-office', 'Ouvrir l''administration et son tableau de bord', 'ADMINISTRATION', TRUE, CURRENT_TIMESTAMP),
('CONSULTER_UTILISATEURS', 'Consulter les utilisateurs', 'Rechercher les comptes et ouvrir leur fiche', 'SECURITY', TRUE, CURRENT_TIMESTAMP),
('SUSPENDRE_UTILISATEURS', 'Suspendre les utilisateurs', 'Suspendre un compte (connexion refusée, session fermée)', 'SECURITY', TRUE, CURRENT_TIMESTAMP),
('REACTIVER_UTILISATEURS', 'Réactiver les utilisateurs', 'Lever la suspension d''un compte', 'SECURITY', TRUE, CURRENT_TIMESTAMP),
('MODERER_ESPACES', 'Modérer les espaces', 'Suspendre ou réactiver n''importe quel espace professionnel', 'PROFESSIONAL', TRUE, CURRENT_TIMESTAMP),
('MODERER_OFFRES', 'Modérer les offres', 'Suspendre ou republier n''importe quelle offre', 'CATALOGUE', TRUE, CURRENT_TIMESTAMP),
('MODERER_AVIS', 'Modérer les avis', 'Masquer ou rétablir un avis', 'SEARCH', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- 2. Descriptions précisées : « gérer » ses propres données ≠ modérer celles des autres.
UPDATE permissions SET description = v.description
FROM (VALUES
    ('GERER_ROLES', 'Donner ou retirer les rôles administratifs d''un compte'),
    ('GERER_PERMISSIONS', 'Modifier les permissions de chaque rôle'),
    ('GERER_ESPACES', 'Créer et modifier ses propres espaces professionnels'),
    ('GERER_OFFRES', 'Créer, modifier et publier ses propres offres'),
    ('GERER_AVIS', 'Écrire et modifier ses propres avis'),
    ('GERER_SIGNALEMENTS', 'Traiter les signalements envoyés par les utilisateurs'),
    ('GERER_JOURNAL', 'Consulter le journal des actions du back-office')
) AS v(code, description)
WHERE permissions.code = v.code;

-- 3. GERER_UTILISATEURS mélangeait consulter, suspendre et supprimer : remplacée par les trois
--    permissions ci-dessus. Aucune liaison ne l'utilisait.
DELETE FROM permissions WHERE code = 'GERER_UTILISATEURS';

-- 4. Matrice par défaut rôle → permissions.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM (VALUES
    -- Tout compte : utilisation courante de Nexora
    ('UTILISATEUR', 'CONSULTER_RECHERCHE'), ('UTILISATEUR', 'GERER_FAVORIS'), ('UTILISATEUR', 'GERER_AVIS'),
    ('UTILISATEUR', 'GERER_CONVERSATIONS'), ('UTILISATEUR', 'GERER_MESSAGES'), ('UTILISATEUR', 'GERER_NOTIFICATIONS'),
    ('UTILISATEUR', 'GERER_PANIERS'), ('UTILISATEUR', 'GERER_COMMANDES'), ('UTILISATEUR', 'GERER_RESERVATIONS'),
    -- Professionnel : ses espaces et ses offres (la propriété est vérifiée par chaque service)
    ('FOURNISSEUR', 'GERER_ESPACES'), ('FOURNISSEUR', 'GERER_HORAIRES'), ('FOURNISSEUR', 'GERER_OFFRES'),
    ('FOURNISSEUR', 'GERER_PROMOTIONS'), ('FOURNISSEUR', 'GERER_IMAGES'),
    -- Agent de vérification : hors back-office
    ('AGENT_VERIFICATION', 'TRAITER_VERIFICATIONS'),
    -- Support
    ('SUPPORT', 'ACCEDER_BACK_OFFICE'), ('SUPPORT', 'CONSULTER_UTILISATEURS'), ('SUPPORT', 'REACTIVER_UTILISATEURS'),
    -- Modérateur
    ('MODERATEUR', 'ACCEDER_BACK_OFFICE'), ('MODERATEUR', 'CONSULTER_UTILISATEURS'), ('MODERATEUR', 'MODERER_ESPACES'),
    ('MODERATEUR', 'MODERER_OFFRES'), ('MODERATEUR', 'MODERER_AVIS'), ('MODERATEUR', 'GERER_SIGNALEMENTS'),
    -- Gestionnaire
    ('GESTIONNAIRE', 'ACCEDER_BACK_OFFICE'), ('GESTIONNAIRE', 'GERER_CATEGORIES'), ('GESTIONNAIRE', 'GERER_TYPES_OFFRES'),
    ('GESTIONNAIRE', 'GERER_ATTRIBUTS'), ('GESTIONNAIRE', 'GERER_PARAMETRES'), ('GESTIONNAIRE', 'VOIR_STATISTIQUES')
) AS m(role_code, permission_code)
JOIN roles r ON r.code = m.role_code
JOIN permissions p ON p.code = m.permission_code
ON CONFLICT DO NOTHING;

-- Administrateur : tout le back-office sauf les rôles et les permissions eux-mêmes.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'ACCEDER_BACK_OFFICE', 'CONSULTER_UTILISATEURS', 'SUSPENDRE_UTILISATEURS', 'REACTIVER_UTILISATEURS',
    'MODERER_ESPACES', 'MODERER_OFFRES', 'MODERER_AVIS', 'GERER_SIGNALEMENTS',
    'SUPERVISER_VERIFICATIONS', 'GERER_AGENTS_VERIFICATION', 'GERER_CERTIFICATIONS',
    'GERER_CATEGORIES', 'GERER_TYPES_OFFRES', 'GERER_ATTRIBUTS', 'GERER_PARAMETRES', 'VOIR_STATISTIQUES',
    'GERER_JOURNAL')
WHERE r.code IN ('ADMIN', 'SUPER_ADMIN')
ON CONFLICT DO NOTHING;

-- Super administrateur : en plus, les rôles administratifs et la matrice des permissions.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('GERER_ROLES', 'GERER_PERMISSIONS')
WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- 5. Tout compte a le rôle UTILISATEUR (donné à l'inscription par auth-service) : on le
--    rétablit pour les comptes créés directement en SQL (administrateur initial, démonstration).
INSERT INTO account_roles (account_id, role_id)
SELECT a.id, r.id FROM accounts a JOIN roles r ON r.code = 'UTILISATEUR'
ON CONFLICT DO NOTHING;
