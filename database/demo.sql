-- ============================================================================
-- Données de DÉMONSTRATION de Nexora, en une seule commande (hors install.sql).
--
-- 7 espaces avec leurs comptes, adresses et offres ; comptes de démonstration du
-- back-office et agent de vérification ; avis réels et horaires des espaces.
-- Mot de passe de tous les comptes : Password1!
-- Rejouable : rien n'est créé deux fois. Vos propres données ne sont pas modifiées
-- (seule la note des espaces est recalculée à partir de leurs avis visibles).
--
-- Utilisation, depuis le dossier database/, APRÈS install.sql ou mise_a_jour.sql :
--   psql -U postgres -d nexora_marketplace -v ON_ERROR_STOP=1 -f demo.sql
-- Ensuite, pour les photos : les packs locaux (offres_demo_local.sql puis v2), puis
-- 09_seed/29_demo_caracteristiques.sql pour ranger leurs offres et renseigner leurs caractéristiques.
-- ============================================================================

-- Les scripts sont enregistrés en UTF-8 : sans cette ligne, psql sous Windows les lit
-- en WIN1252 et abîme les accents (« journÃ©e »).
SET client_encoding = 'UTF8';

\echo '== Espaces et offres de démonstration'
\i 09_seed/07_demo_espaces.sql
\echo '== Comptes de vérification et du back-office'
\i 09_seed/22_demo_verification.sql
\i 09_seed/23_demo_administration.sql
\echo '== Avis, horaires et caractéristiques des articles'
\i 09_seed/25_demo_avis.sql
\i 09_seed/26_demo_horaires.sql
\i 09_seed/33_demo_reponses_avis.sql
\i 09_seed/34_demo_popularite.sql
\i 09_seed/35_demo_statistiques.sql
\i 09_seed/29_demo_caracteristiques.sql
\echo '== Démonstration installée'
