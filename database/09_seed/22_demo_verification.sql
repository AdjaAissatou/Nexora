-- ============================================================================
-- Comptes de démonstration pour la vérification des espaces (comme
-- 07_demo_espaces.sql : hors install.sql, à jouer seulement en démo/test).
-- Mot de passe : "Password1!" (même hash BCrypt que 07_demo_espaces.sql).
--   agent.verification@nexora-demo.sn  → UTILISATEUR + AGENT_VERIFICATION
--   admin.demo@nexora-demo.sn          → UTILISATEUR + ADMIN
-- Rejouable sans doublon.
-- ============================================================================

INSERT INTO accounts (first_name, last_name, email, phone, password, enabled, verified, locked, created_at)
VALUES
('Awa', 'Ndiaye', 'agent.verification@nexora-demo.sn', '770000101',
 '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP),
('Moussa', 'Faye', 'admin.demo@nexora-demo.sn', '770000102',
 '$2b$10$6s440w6aNTBkWGdZgZv7/O1ZrLk1woAspcZPThyCSkW0VpMomE3e6', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

INSERT INTO utilisateurs (account_id, nom, prenom, email, telephone, email_verifie, telephone_verifie, langue, statut, actif)
SELECT a.id, a.last_name, a.first_name, a.email, a.phone, TRUE, TRUE, 'FRANCAIS', 'ACTIF', TRUE
FROM accounts a
WHERE a.email IN ('agent.verification@nexora-demo.sn', 'admin.demo@nexora-demo.sn')
ON CONFLICT (email) DO NOTHING;

INSERT INTO account_roles (account_id, role_id)
SELECT a.id, r.id
FROM accounts a
JOIN roles r ON (a.email, r.code) IN (
    ('agent.verification@nexora-demo.sn', 'UTILISATEUR'),
    ('agent.verification@nexora-demo.sn', 'AGENT_VERIFICATION'),
    ('admin.demo@nexora-demo.sn', 'UTILISATEUR'),
    ('admin.demo@nexora-demo.sn', 'ADMIN'))
ON CONFLICT DO NOTHING;
