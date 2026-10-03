-- ============================================================================
-- MISE À JOUR d'une base Nexora EXISTANTE, sans perte de données.
--
-- Ne rejouez PAS install.sql sur une base existante : ses scripts de création
-- commencent par DROP TABLE ... CASCADE et effaceraient vos données.
--
-- Ce script applique les évolutions du 30/09/2026, de la vérification des
-- espaces (étape 5a) aux paramètres (étape 6f). Chaque script est rejouable :
-- ce qui existe déjà est conservé, il n'y a aucun risque à le relancer.
--
-- Utilisation, depuis le dossier database/ :
--   psql -U postgres -d nexora_marketplace -v ON_ERROR_STOP=1 -f mise_a_jour.sql
-- ============================================================================

\echo '== Structure : vérification, modération, horaires, avis'
\i 03_professional/07_verification.sql
\i 03_professional/08_moderation.sql
\i 03_professional/09_horaires.sql
\i 05_search/07_moderation_avis_signalements.sql

\echo '== Données de référence : règles de vérification, permissions, paramètres'
\i 09_seed/21_verification.sql
\i 09_seed/24_rbac_permissions.sql
\i 09_seed/27_parametres.sql

\echo '== Démonstration : comptes d''administration et agent, avis, horaires'
\i 09_seed/22_demo_verification.sql
\i 09_seed/23_demo_administration.sql
\i 09_seed/25_demo_avis.sql
\i 09_seed/26_demo_horaires.sql

\echo '== Mise à jour terminée'
