-- =====================================================================
--  NEXORA - Donnees de configuration (seed)
--  Demontre la genericite : un nouveau domaine metier s'ajoute
--  UNIQUEMENT par des INSERT, sans aucune modification de code.
-- =====================================================================

-- ---------- Types de champ (pilotent le rendu et le stockage EAV) ----
INSERT INTO type_champ (code, libelle, composant_ui) VALUES
 ('TEXT',       'Texte court',        'inputText'),
 ('LONG_TEXT',  'Texte long',         'inputTextarea'),
 ('NUMBER',     'Nombre',             'inputNumber'),
 ('BOOLEAN',    'Oui / Non',          'selectBooleanCheckbox'),
 ('DATE',       'Date',               'datePicker'),
 ('EMAIL',      'Email',              'inputText'),
 ('PHONE',      'Telephone',          'inputMask'),
 ('URL',        'Lien web',           'inputText'),
 ('LIST',       'Liste (choix unique)','selectOneMenu'),
 ('MULTI_LIST', 'Liste (choix multiple)','selectManyCheckbox');

-- ---------- Profils & roles (RBAC) -----------------------------------
INSERT INTO profile (libelle) VALUES
 ('VISITEUR'), ('CLIENT'), ('PROFESSIONNEL'), ('VENDEUR'), ('PRESTATAIRE'),
 ('LIVREUR'), ('MODERATEUR'), ('SUPPORT'), ('CONTROLEUR_QUALITE'),
 ('GESTIONNAIRE_FINANCIER'), ('ADMIN'), ('SUPER_ADMIN');

INSERT INTO role (libelle, date_attribution) VALUES
 ('ROLE_CLIENT', now()), ('ROLE_PRO', now()), ('ROLE_ADMIN', now()),
 ('ROLE_MODERATION', now()), ('ROLE_FINANCE', now());

INSERT INTO permission (libelle, description) VALUES
 ('OFFRE_CREER',       'Publier une offre'),
 ('OFFRE_MODIFIER',    'Modifier une offre'),
 ('OFFRE_SUPPRIMER',   'Supprimer une offre'),
 ('COMMANDE_VALIDER',  'Valider une commande'),
 ('ESPACE_VERIFIER',   'Verifier un espace professionnel'),
 ('LITIGE_ARBITRER',   'Arbitrer un litige'),
 ('UTILISATEUR_GERER', 'Gerer les comptes utilisateurs'),
 ('FINANCE_CONSULTER', 'Consulter les rapports financiers');

-- ---------- Types d'offre --------------------------------------------
INSERT INTO type_offre (libelle) VALUES ('PRODUIT'), ('SERVICE');

-- ---------- Types de categorie ---------------------------------------
INSERT INTO type_categorie (code, libelle) VALUES
 ('ELECTRONIQUE', 'Electronique'),
 ('IMMOBILIER',   'Immobilier'),
 ('AUTOMOBILE',   'Automobile'),
 ('SANTE',        'Sante'),
 ('BATIMENT',     'Batiment & Artisanat'),
 ('RESTAURATION', 'Restauration'),
 ('TRANSPORT',    'Transport & Livraison');

-- ---------- Types d'espace professionnel -----------------------------
INSERT INTO type_espace_professionnel (code, libelle) VALUES
 ('BOUTIQUE','Boutique'), ('CABINET','Cabinet'), ('CLINIQUE','Clinique'),
 ('RESTAURANT','Restaurant'), ('GARAGE','Garage'), ('PHARMACIE','Pharmacie'),
 ('ATELIER','Atelier'), ('AGENCE','Agence'), ('SUPERMARCHE','Supermarche');

-- =====================================================================
--  EXEMPLE 1 : domaine PRODUIT "Ordinateur portable"
--  On cree la categorie puis ses attributs dynamiques.
-- =====================================================================
INSERT INTO categorie_offre (nom, slug, niveau, id_type_categorie)
 VALUES ('Ordinateurs portables', 'ordinateurs-portables', 0,
         (SELECT id_type_categorie FROM type_categorie WHERE code='ELECTRONIQUE'));

INSERT INTO attribut (nom, unite, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT 'Marque', NULL, TRUE, TRUE, c.id_categorie, t.id_type_champ
   FROM categorie_offre c, type_champ t
  WHERE c.slug='ordinateurs-portables' AND t.code='LIST';
INSERT INTO attribut (nom, unite, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT 'Memoire RAM', 'Go', TRUE, TRUE, c.id_categorie, t.id_type_champ
   FROM categorie_offre c, type_champ t
  WHERE c.slug='ordinateurs-portables' AND t.code='NUMBER';
INSERT INTO attribut (nom, unite, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT 'Stockage SSD', 'Go', FALSE, TRUE, c.id_categorie, t.id_type_champ
   FROM categorie_offre c, type_champ t
  WHERE c.slug='ordinateurs-portables' AND t.code='NUMBER';

-- Valeurs possibles de l'attribut "Marque"
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut
   FROM attribut a
   JOIN (VALUES ('Dell',1),('HP',2),('Lenovo',3),('Apple',4),('Asus',5)) AS v(valeur, ordre) ON TRUE
  WHERE a.nom='Marque'
    AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='ordinateurs-portables');

-- =====================================================================
--  EXEMPLE 2 : domaine SERVICE "Plomberie"
--  Meme mecanique, aucun code specifique.
-- =====================================================================
INSERT INTO categorie_offre (nom, slug, niveau, id_type_categorie)
 VALUES ('Plomberie', 'plomberie', 0,
         (SELECT id_type_categorie FROM type_categorie WHERE code='BATIMENT'));

INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT 'Type d''intervention', TRUE, TRUE, c.id_categorie, t.id_type_champ
   FROM categorie_offre c, type_champ t
  WHERE c.slug='plomberie' AND t.code='LIST';
INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT 'Intervention urgente 24/7', FALSE, TRUE, c.id_categorie, t.id_type_champ
   FROM categorie_offre c, type_champ t
  WHERE c.slug='plomberie' AND t.code='BOOLEAN';

INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut
   FROM attribut a
   JOIN (VALUES ('Fuite d''eau',1),('Installation sanitaire',2),
                ('Debouchage',3),('Chauffe-eau',4)) AS v(valeur, ordre) ON TRUE
  WHERE a.nom='Type d''intervention'
    AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='plomberie');

-- ---------- Modes & statuts de paiement ------------------------------
INSERT INTO mode_paiement (libelle) VALUES
 ('ESPECES'), ('CARTE_BANCAIRE'), ('MOBILE_MONEY'), ('WALLET'), ('VIREMENT');
INSERT INTO statut_paiement (libelle) VALUES
 ('INITIE'), ('AUTORISE'), ('REGLE'), ('ECHOUE'), ('REMBOURSE');

-- ---------- Types de notification ------------------------------------
INSERT INTO type_notification (libelle) VALUES
 ('COMMANDE'), ('MESSAGE'), ('PROMOTION'), ('AVIS'), ('SYSTEME'), ('LITIGE');
