/**************************************************************************
 * SYNCHRONISATION DU ROLE FOURNISSEUR
 * Règle : un compte a le rôle FOURNISSEUR si et seulement s'il possède au
 * moins un espace professionnel (docs/architecture-acteurs.md §1).
 * L'application maintient cette règle à la création/suppression d'un espace ;
 * ce script la rétablit pour les comptes créés avant, ou dont les espaces ont
 * été insérés directement en SQL. Idempotent : peut être relancé sans risque.
 **************************************************************************/

INSERT INTO account_roles (account_id, role_id)
SELECT DISTINCT u.account_id, r.id
FROM espace_professionnel e
JOIN utilisateurs u ON u.id_utilisateur = e.id_utilisateur
JOIN roles r ON r.code = 'FOURNISSEUR'
WHERE u.account_id IS NOT NULL
ON CONFLICT (account_id, role_id) DO NOTHING;

DELETE FROM account_roles ar
USING roles r
WHERE ar.role_id = r.id
  AND r.code = 'FOURNISSEUR'
  AND NOT EXISTS (
      SELECT 1
      FROM espace_professionnel e
      JOIN utilisateurs u ON u.id_utilisateur = e.id_utilisateur
      WHERE u.account_id = ar.account_id
  );
