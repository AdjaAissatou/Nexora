INSERT INTO roles
(code, name, description, active)
VALUES
(
    'SUPER_ADMIN',
    'Super Administrateur',
    'Accès total à la plateforme Nexora',
    TRUE
),
(
    'ADMIN',
    'Administrateur',
    'Gestion complète de l’administration',
    TRUE
),
(
    'MODERATEUR',
    'Modérateur',
    'Modération des contenus et des signalements',
    TRUE
),
(
    'UTILISATEUR',
    'Utilisateur',
    'Compte standard de consultation et d’achat',
    TRUE
),
(
    'FOURNISSEUR',
    'Fournisseur',
    'Compte ayant au moins un espace professionnel',
    TRUE
),
(
    'SUPPORT',
    'Support',
    'Assistance et aide aux utilisateurs',
    TRUE
),
(
    'GESTIONNAIRE',
    'Gestionnaire',
    'Gestion opérationnelle de la plateforme',
    TRUE
)
ON CONFLICT (code) DO NOTHING;