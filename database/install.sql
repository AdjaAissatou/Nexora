/**************************************************************************
 * NEXORA MARKETPLACE
 * INSTALLATION COMPLETE
 **************************************************************************/

-------------------------------------------------------
-- INITIALISATION
-------------------------------------------------------

\i 00_init/01_types.sql
\i 00_init/03_functions.sql

-------------------------------------------------------
-- SECURITE
-------------------------------------------------------

\i 01_security/01_roles.sql
\i 01_security/02_permissions.sql
\i 01_security/03_role_permissions.sql
\i 01_security/06_accounts.sql
\i 01_security/07_account_roles.sql
\i 01_security/04_utilisateurs.sql
\i 01_security/08_otp_codes.sql

-------------------------------------------------------
-- PROFESSIONNEL
-------------------------------------------------------

\i 03_professional/01_type_espace.sql
\i 03_professional/02_espace_professionnel.sql
\i 03_professional/03_adresse.sql
\i 03_professional/04_horaire.sql
\i 03_professional/05_certification.sql


-------------------------------------------------------
-- SHARED
-------------------------------------------------------

\i 02_shared/01_media.sql
\i 02_shared/02_localisation.sql
\i 02_shared/03_notification.sql
\i 02_shared/04_moyen_contact.sql
\i 02_shared/05_horaire_exception.sql
\i 02_shared/06_jour_ferie.sql

-------------------------------------------------------
-- CATALOGUE
-------------------------------------------------------

\i 04_catalogue/01_categorie.sql
\i 04_catalogue/02_type_offre.sql
\i 04_catalogue/03_offre.sql
\i 04_catalogue/04_produit.sql
\i 04_catalogue/05_service.sql
\i 04_catalogue/06_attribut.sql
\i 04_catalogue/07_valeur_attribut_possible.sql
\i 04_catalogue/08_offre_attribut.sql
\i 04_catalogue/09_image.sql
\i 04_catalogue/10_promotion.sql
\i 04_catalogue/11_tag.sql
\i 04_catalogue/12_offre_tag.sql
\i 04_catalogue/13_ressource.sql
\i 04_catalogue/14_disponibilite.sql

-------------------------------------------------------
-- RECHERCHE
-------------------------------------------------------

\i 05_search/01_favori.sql
\i 05_search/02_historique_recherche.sql
\i 05_search/03_historique_consultation.sql
\i 05_search/04_recherche_sauvegardee.sql
\i 05_search/05_signalement.sql
\i 05_search/06_avis.sql

-------------------------------------------------------
-- COMMUNICATION
-------------------------------------------------------

\i 06_communication/01_conversation.sql
\i 06_communication/02_participant_conversation.sql
\i 06_communication/03_message.sql
\i 06_communication/04_piece_jointe.sql

-------------------------------------------------------
-- COMMERCE
-------------------------------------------------------

\i 07_commerce/00_adresse_utilisateur.sql
\i 07_commerce/01_panier.sql
\i 07_commerce/02_ligne_panier.sql
\i 07_commerce/03_commande.sql
\i 07_commerce/04_sous_commande.sql
\i 07_commerce/05_ligne_commande.sql
\i 07_commerce/06_reservation.sql
\i 07_commerce/07_livraison.sql
\i 07_commerce/08_paiement.sql
\i 07_commerce/09_transaction.sql
\i 07_commerce/10_paiement_sous_commande.sql
\i 07_commerce/11_escrow.sql
\i 07_commerce/12_wallet.sql
\i 07_commerce/13_litige.sql
\i 07_commerce/14_facture.sql

-------------------------------------------------------
-- ADMINISTRATION
-------------------------------------------------------

\i 08_administration/01_statistique.sql
\i 08_administration/02_journal_action.sql
\i 08_administration/03_parametre.sql

-------------------------------------------------------
-- DONNEES INITIALES
-------------------------------------------------------

\i 09_seed/01_roles.sql
\i 09_seed/02_permissions.sql
\i 09_seed/03_categories.sql
\i 09_seed/04_types_espaces.sql
\i 09_seed/05_types_offres.sql
\i 09_seed/06_admin.sql
\i 09_seed/08_sous_categories.sql
\i 09_seed/09_sous_categories_informatique.sql

-------------------------------------------------------
-- VUES
-------------------------------------------------------

\i 10_views/01_recherche_globale.sql
\i 10_views/02_dashboard_admin.sql
\i 10_views/03_dashboard_fournisseur.sql
\i 10_views/04_dashboard_utilisateur.sql
\i 10_views/05_statistiques.sql
\i 00_init/04_indexes.sql

\echo '========================================='
\echo ' INSTALLATION NEXORA TERMINEE'
\echo '========================================='