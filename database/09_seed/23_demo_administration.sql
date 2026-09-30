-- ============================================================================
-- Comptes de démonstration du back-office, un par rôle administratif
-- (docs/architecture-acteurs.md §9.2). Hors install.sql, comme 07_demo_espaces.sql.
-- Mot de passe : "Password1!" (même hash BCrypt que 07_demo_espaces.sql).
--   superadmin.demo@nexora-demo.sn    → SUPER_ADMIN
--   moderateur.demo@nexora-demo.sn    → MODERATEUR
--   support.demo@nexora-demo.sn       → SUPPORT
--   gestionnaire.demo@nexora-demo.sn  → GESTIONNAIRE
-- (admin.demo@nexora-demo.sn → ADMIN est dans 22_demo_verification.sql.) Rejouable.
-- ============================================================================

INSERT INTO accounts (first_name, last_name, email, phone, password, enabled, verified, locked, created_at)
VALUES
('Mariama', 'Sow', 'superadmin.demo@nexora-demo.sn', '770000103',
 '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP),
('Lamine', 'Diallo', 'moderateur.demo@nexora-demo.sn', '770000104',
 '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP),
('Khady', 'Mbaye', 'support.demo@nexora-demo.sn', '770000105',
 '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP),
('Pape', 'Gueye', 'gestionnaire.demo@nexora-demo.sn', '770000106',
 '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO utilisateurs (account_id, nom, prenom, email, telephone, email_verifie, telephone_verifie, langue, statut, actif)
SELECT a.id, a.last_name, a.first_name, a.email, a.phone, TRUE, TRUE, 'FRANCAIS', 'ACTIF', TRUE
FROM accounts a
WHERE a.email IN ('superadmin.demo@nexora-demo.sn', 'moderateur.demo@nexora-demo.sn',
                  'support.demo@nexora-demo.sn', 'gestionnaire.demo@nexora-demo.sn')
ON CONFLICT (email) DO NOTHING;

INSERT INTO account_roles (account_id, role_id)
SELECT a.id, r.id
FROM accounts a
JOIN roles r ON (a.email, r.code) IN (
    ('superadmin.demo@nexora-demo.sn', 'UTILISATEUR'),
    ('superadmin.demo@nexora-demo.sn', 'SUPER_ADMIN'),
    ('moderateur.demo@nexora-demo.sn', 'UTILISATEUR'),
    ('moderateur.demo@nexora-demo.sn', 'MODERATEUR'),
    ('support.demo@nexora-demo.sn', 'UTILISATEUR'),
    ('support.demo@nexora-demo.sn', 'SUPPORT'),
    ('gestionnaire.demo@nexora-demo.sn', 'UTILISATEUR'),
    ('gestionnaire.demo@nexora-demo.sn', 'GESTIONNAIRE'))
ON CONFLICT DO NOTHING;
