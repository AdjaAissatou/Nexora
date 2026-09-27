INSERT INTO accounts
(
    first_name,
    last_name,
    email,
    phone,
    password,
    enabled,
    verified,
    locked,
    created_at
)
VALUES
(
    'Nexora',
    'Admin',
    'admin@nexora.com',
    '+221700000000',
    '$2a$10$REPLACE_THIS_WITH_BCRYPT_HASH',
    TRUE,
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;
INSERT INTO account_roles
(
    account_id,
    role_id
)
SELECT
    a.id,
    r.id
FROM accounts a
JOIN roles r
    ON r.code = 'SUPER_ADMIN'
WHERE a.email = 'admin@nexora.com'
ON CONFLICT DO NOTHING;
INSERT INTO utilisateurs
(
    account_id,
    nom,
    prenom,
    pseudo,
    email,
    telephone,
    email_verifie,
    telephone_verifie,
    langue,
    double_auth,
    tentative_connexion,
    compte_bloque,
    statut,
    actif
)
SELECT
    a.id,
    a.last_name,
    a.first_name,
    'admin.nexora',
    a.email,
    a.phone,
    TRUE,
    TRUE,
    'FRANCAIS',
    FALSE,
    0,
    FALSE,
    'ACTIF',
    TRUE
FROM accounts a
WHERE a.email = 'admin@nexora.com'
ON CONFLICT (email) DO NOTHING;