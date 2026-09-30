-- ============================================================================
-- Paramètres de Nexora (docs/architecture-acteurs.md §9.12).
--
-- Chaque paramètre ci-dessous est lu par le code : le modifier depuis le
-- back-office (/admin/parametres, permission GERER_PARAMETRES) change réellement
-- le comportement de la plateforme. Les bornes de chaque valeur sont vérifiées
-- par administration-service (ParametresConnus).
--
-- Rejouable : les valeurs déjà modifiées ne sont pas écrasées.
-- ============================================================================

INSERT INTO parametre (code, libelle, description, valeur_texte, valeur_numerique, valeur_booleenne, categorie, module, actif, date_creation)
VALUES
('SITE_BANDEAU', 'Bandeau d''annonce',
 'Message affiché en haut de toutes les pages publiques (maintenance, nouveauté…). Vide : aucun bandeau.',
 NULL, NULL, NULL, 'SITE', 'WEB', TRUE, CURRENT_TIMESTAMP),
('SITE_CONTACT_EMAIL', 'E-mail de contact', 'Adresse affichée dans le pied de page de Nexora.',
 'contact@nexora.sn', NULL, NULL, 'SITE', 'WEB', TRUE, CURRENT_TIMESTAMP),
('SITE_CONTACT_TELEPHONE', 'Téléphone de contact', 'Numéro affiché dans le pied de page de Nexora.',
 '33 800 00 00', NULL, NULL, 'SITE', 'WEB', TRUE, CURRENT_TIMESTAMP),
('INSCRIPTIONS_OUVERTES', 'Inscriptions ouvertes',
 'Si non, la création de nouveaux comptes est refusée (les comptes existants se connectent normalement).',
 NULL, NULL, TRUE, 'COMPTES', 'AUTH', TRUE, CURRENT_TIMESTAMP),
('ESPACES_MAX_PAR_COMPTE', 'Espaces par compte (maximum)',
 'Nombre maximal d''espaces professionnels qu''un même compte peut créer.',
 NULL, 5, NULL, 'ESPACES', 'ESPACE', TRUE, CURRENT_TIMESTAMP),
('AVIS_LONGUEUR_MAX', 'Longueur maximale d''un avis', 'Nombre maximal de caractères du commentaire d''un avis.',
 NULL, 1000, NULL, 'AVIS', 'RECHERCHE', TRUE, CURRENT_TIMESTAMP),
('VERIFICATION_DELAI_JOURS', 'Délai habituel de vérification (jours)',
 'Délai annoncé au professionnel pour le traitement de sa demande de vérification.',
 NULL, 5, NULL, 'VERIFICATION', 'ESPACE', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;
