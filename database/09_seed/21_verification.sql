-- ============================================================================
-- Vérification des espaces : rôle, permissions, justificatifs et règles par
-- défaut (docs/architecture-acteurs.md §8). Rejouable sans doublon.
-- ============================================================================

-- Agent de vérification : rôle distinct, sans accès au reste de l'administration.
INSERT INTO roles (code, name, description, active)
VALUES ('AGENT_VERIFICATION', 'Agent de vérification',
        'Examine les demandes de vérification des espaces professionnels et décide', TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (code, name, description, module, active, created_at)
VALUES
('TRAITER_VERIFICATIONS', 'Traiter les vérifications',
 'Prendre en charge, examiner, approuver ou refuser une demande de vérification', 'PROFESSIONAL', TRUE, CURRENT_TIMESTAMP),
('SUPERVISER_VERIFICATIONS', 'Superviser les vérifications',
 'Voir toutes les demandes, réattribuer, annuler, révoquer', 'PROFESSIONAL', TRUE, CURRENT_TIMESTAMP),
('GERER_AGENTS_VERIFICATION', 'Gérer les agents de vérification',
 'Donner ou retirer le rôle d''agent de vérification', 'PROFESSIONAL', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON (r.code, p.code) IN (
    ('AGENT_VERIFICATION', 'TRAITER_VERIFICATIONS'),
    ('ADMIN', 'SUPERVISER_VERIFICATIONS'),
    ('SUPER_ADMIN', 'SUPERVISER_VERIFICATIONS'),
    ('ADMIN', 'GERER_AGENTS_VERIFICATION'),
    ('SUPER_ADMIN', 'GERER_AGENTS_VERIFICATION'))
ON CONFLICT DO NOTHING;

-- Référentiel des justificatifs.
INSERT INTO type_justificatif (code, libelle, description)
VALUES
('PIECE_IDENTITE', 'Pièce d''identité du responsable', 'Carte nationale d''identité ou passeport, en cours de validité'),
('JUSTIFICATIF_ADRESSE', 'Justificatif d''adresse', 'Facture Senelec ou SEN''EAU, bail, certificat de résidence'),
('PHOTO_DEVANTURE', 'Photo de la devanture', 'Photo de l''établissement où l''enseigne est lisible'),
('NINEA', 'Attestation NINEA', 'Numéro d''identification national des entreprises et associations'),
('RCCM', 'Registre du commerce (RCCM)', 'Extrait du registre du commerce et du crédit mobilier'),
('AUTORISATION_EXERCICE', 'Autorisation d''exercer', 'Agrément ou autorisation délivrée par l''autorité compétente'),
('DIPLOME', 'Diplôme', 'Diplôme ou titre professionnel du responsable'),
('AUTRE', 'Autre document', 'Tout autre document utile à la vérification')
ON CONFLICT (code) DO NOTHING;

-- Règles par défaut, pour tous les types d'espace :
-- pièce d'identité obligatoire ; justificatif d'adresse OU photo de la devanture ;
-- NINEA et RCCM facultatifs (le secteur informel doit pouvoir être vérifié).
INSERT INTO justificatif_requis (id_type_espace, id_type_justificatif, obligatoire, groupe_alternatif)
SELECT NULL, tj.id_type_justificatif, r.obligatoire, r.groupe
FROM (VALUES
    ('PIECE_IDENTITE', TRUE, NULL),
    ('JUSTIFICATIF_ADRESSE', TRUE, 'ADRESSE'),
    ('PHOTO_DEVANTURE', TRUE, 'ADRESSE'),
    ('NINEA', FALSE, NULL),
    ('RCCM', FALSE, NULL)
) AS r(code, obligatoire, groupe)
JOIN type_justificatif tj ON tj.code = r.code
ON CONFLICT DO NOTHING;

-- Établissements réglementés : autorisation d'exercer obligatoire en plus.
INSERT INTO justificatif_requis (id_type_espace, id_type_justificatif, obligatoire, groupe_alternatif)
SELECT te.id_type_espace, tj.id_type_justificatif, TRUE, NULL
FROM type_espace te
JOIN type_justificatif tj ON tj.code = 'AUTORISATION_EXERCICE'
WHERE te.nom IN ('Pharmacie', 'Clinique', 'École', 'Université')
ON CONFLICT DO NOTHING;

-- Reprise de l'existant : un espace déjà marqué vérifié avant ce processus reçoit
-- une demande APPROUVEE qui le dit, pour que « verifie » corresponde toujours à
-- une décision tracée.
INSERT INTO verification_espace (id_espace, id_demandeur, statut, motif, date_soumission, date_decision)
SELECT e.id_espace, e.id_utilisateur, 'APPROUVEE',
       'Espace vérifié avant la mise en place du processus de vérification',
       COALESCE(e.date_verification, e.date_creation), COALESCE(e.date_verification, e.date_creation)
FROM espace_professionnel e
WHERE e.verifie = TRUE
AND NOT EXISTS (SELECT 1 FROM verification_espace v WHERE v.id_espace = e.id_espace);

INSERT INTO verification_evenement (id_verification, type, ancien_statut, nouveau_statut, role_acteur, commentaire, date_evenement)
SELECT v.id_verification, 'REPRISE', NULL, 'APPROUVEE', 'SYSTEME', v.motif, v.date_decision
FROM verification_espace v
WHERE v.statut = 'APPROUVEE' AND v.id_agent IS NULL
AND NOT EXISTS (SELECT 1 FROM verification_evenement ev WHERE ev.id_verification = v.id_verification);

UPDATE espace_professionnel e
SET date_verification = v.date_decision
FROM verification_espace v
WHERE v.id_espace = e.id_espace AND v.statut = 'APPROUVEE' AND e.date_verification IS NULL;

-- Droits de l'utilisateur applicatif (les tables sont créées par postgres).
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'nexora_user') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON
            type_justificatif, justificatif_requis, verification_espace,
            verification_document, verification_controle, verification_evenement
            TO nexora_user;
        GRANT USAGE, SELECT ON
            type_justificatif_id_type_justificatif_seq,
            justificatif_requis_id_justificatif_requis_seq,
            verification_espace_id_verification_seq,
            verification_document_id_document_seq,
            verification_controle_id_controle_seq,
            verification_evenement_id_evenement_seq
            TO nexora_user;
    END IF;
END $$;
