-- ============================================================================
-- MISE À JOUR d'une base Nexora EXISTANTE, sans perte de données.
--
-- Ne rejouez PAS install.sql sur une base existante : ses scripts de création
-- commencent par DROP TABLE ... CASCADE et effaceraient vos données.
--
-- Ce script applique toutes les évolutions depuis l'installation initiale du
-- 27/09/2026 : géographie, catalogue détaillé, lieux publics (28-29/09), puis
-- vérification des espaces, back-office, horaires et paramètres (30/09).
-- Les tables ajoutées ne sont créées que si elles manquent ; les données de
-- référence sont insérées sans doublon. Il n'y a aucun risque à le relancer.
--
-- Utilisation, depuis le dossier database/ :
--   psql -U postgres -d nexora_marketplace -v ON_ERROR_STOP=1 -f mise_a_jour.sql
-- ============================================================================

-- Les scripts sont enregistrés en UTF-8 : sans cette ligne, psql sous Windows les lit
-- en WIN1252 et abîme les accents (« journÃ©e »).
SET client_encoding = 'UTF8';

\echo '== Tables ajoutées les 28 et 29/09 (créées seulement si elles manquent)'
SELECT to_regclass('public.region') IS NULL AS manque \gset
\if :manque
  \i 02_shared/07_region.sql
\endif
SELECT to_regclass('public.departement') IS NULL AS manque \gset
\if :manque
  \i 02_shared/08_departement.sql
\endif
SELECT to_regclass('public.commune') IS NULL AS manque \gset
\if :manque
  \i 02_shared/09_commune.sql
\endif
SELECT to_regclass('public.photo_espace') IS NULL AS manque \gset
\if :manque
  \i 03_professional/06_photo_espace.sql
\endif
SELECT to_regclass('public.lieu_public') IS NULL AS manque \gset
\if :manque
  \i 02_shared/10_lieu_public.sql
\endif
SELECT to_regclass('public.categorie_type_espace') IS NULL AS manque \gset
\if :manque
  \i 04_catalogue/15_categorie_type_espace.sql
\endif
\i 11_migrations/01_colonnes_28_09.sql
\i 01_security/09_revoked_tokens.sql
\i 04_catalogue/16_offre_espace_cascade.sql

\echo '== Référentiels du 28 et 29/09 : catalogue, géographie, lieux publics'
-- Ces référentiels ne sont pas rejouables sur une base déjà à jour : les sous-catégories
-- sont rattachées par nom de parent, et la taxonomie du 29/09 a créé d'autres catégories
-- de même nom (rejoués, ils dupliqueraient des branches). Chacun ne s'exécute donc que si
-- la base n'a pas encore ces données.
SELECT NOT EXISTS (SELECT 1 FROM categorie WHERE id_categorie_parent IS NOT NULL) AS manque \gset
\if :manque
  \echo '   catalogue hiérarchique : ajout'
  \i 09_seed/08_sous_categories.sql
  \i 09_seed/09_sous_categories_informatique.sql
  \i 09_seed/10_sous_categories_reste.sql
  \i 09_seed/11_marques.sql
  \i 09_seed/12_type_offre_autre.sql
  \i 09_seed/14_taxonomie_domaines.sql
  \i 09_seed/15_type_offre_domaines.sql
  \i 09_seed/16_type_offre_attributs_tags.sql
  \i 09_seed/12_type_offre_autre.sql
\else
  \echo '   catalogue hiérarchique : déjà présent'
\endif
SELECT NOT EXISTS (SELECT 1 FROM commune) AS manque \gset
\if :manque
  \i 09_seed/13_geo_senegal.sql
\endif
SELECT NOT EXISTS (SELECT 1 FROM categorie_type_espace) AS manque \gset
\if :manque
  \i 09_seed/17_categorie_type_espace.sql
\endif
-- Les deux scripts de lieux publics n'ont pas de garde anti-doublon : chacun ne
-- s'exécute que si son premier lieu manque.
SELECT NOT EXISTS (SELECT 1 FROM lieu_public WHERE nom = 'Marché Sandaga') AS manque \gset
\if :manque
  \i 09_seed/18_lieux_publics.sql
\endif
SELECT NOT EXISTS (SELECT 1 FROM lieu_public WHERE nom = 'Palais de la République') AS manque \gset
\if :manque
  \i 09_seed/19_lieux_publics_extension.sql
\endif
\i 09_seed/20_synchro_role_fournisseur.sql

\echo '== Structure : vérification, modération, horaires, avis'
\i 03_professional/07_verification.sql
\i 03_professional/08_moderation.sql
\i 03_professional/09_horaires.sql
\i 05_search/07_moderation_avis_signalements.sql
\i 04_catalogue/17_image_vecteur.sql

\echo '== Données de référence : règles de vérification, permissions, paramètres'
\i 09_seed/21_verification.sql
\i 09_seed/24_rbac_permissions.sql
\i 09_seed/27_parametres.sql

\echo '== Démonstration : comptes d''administration et agent, avis, horaires'
\i 09_seed/22_demo_verification.sql
\i 09_seed/23_demo_administration.sql
\i 09_seed/25_demo_avis.sql
\i 09_seed/26_demo_horaires.sql

\echo '== Accents abîmés par un ancien passage en WIN1252 (réparés s''il y en a)'
\i 11_migrations/02_reparer_accents.sql

\echo '== Mise à jour terminée'
