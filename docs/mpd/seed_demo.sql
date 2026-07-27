-- =====================================================================
--  NEXORA - Jeu de DEMONSTRATION (annonces reelles avec photos)
--  Objectif : disposer, sur une base fraiche, d'annonces PUBLIEES et
--  geolocalisees, chacune avec une image principale et parfois une
--  galerie, afin que l'application (mobile / REST) affiche de vraies
--  photos sans passer par une publication manuelle.
--
--  Pre-requis (a executer dans l'ordre) :
--     1) schema.sql            (structure)
--     2) seed.sql              (referentiels RBAC / types / EAV)
--     3) seed_catalogue.sql    (categories & attributs)
--     4) seed_demo.sql         (CE FICHIER)
--
--  Idempotent : re-executable sans doublon (ON CONFLICT / NOT EXISTS).
--  Les URLs pointent vers des photos libres (Unsplash) ; en production
--  elles seraient servies par le stockage media de la plateforme.
-- =====================================================================

BEGIN;

-- ---------- Types manquants (hotellerie, commerce, transport) --------
INSERT INTO type_categorie (code, libelle) VALUES
 ('HOTELLERIE', 'Hotellerie'),
 ('COMMERCE',   'Commerce & Distribution')
ON CONFLICT (code) DO NOTHING;

INSERT INTO type_espace_professionnel (code, libelle) VALUES
 ('HOTEL',     'Hotel'),
 ('TRANSPORT', 'Transport')
ON CONFLICT (code) DO NOTHING;

-- ---------- Categories de demonstration (idempotent par slug) --------
INSERT INTO categorie_offre (nom, slug, niveau, populaire, id_type_categorie) VALUES
 ('Restaurants',  'restaurant',  0, TRUE,  (SELECT id_type_categorie FROM type_categorie WHERE code='RESTAURATION')),
 ('Hotels',       'hotels',      0, TRUE,  (SELECT id_type_categorie FROM type_categorie WHERE code='HOTELLERIE')),
 ('Plomberie',    'plomberie',   0, FALSE, (SELECT id_type_categorie FROM type_categorie WHERE code='BATIMENT')),
 ('Supermarches', 'supermarche', 0, TRUE,  (SELECT id_type_categorie FROM type_categorie WHERE code='COMMERCE')),
 ('Pharmacies',   'pharmacie',   0, TRUE,  (SELECT id_type_categorie FROM type_categorie WHERE code='SANTE')),
 ('Transport',    'transport',   0, FALSE, (SELECT id_type_categorie FROM type_categorie WHERE code='TRANSPORT'))
ON CONFLICT (slug) DO NOTHING;

-- ---------- Proprietaire de demonstration ----------------------------
--  Mot de passe non exploitable (empreinte factice) : compte vitrine.
INSERT INTO utilisateur (nom, prenom, email, telephone, mot_de_passe, statut_compte, email_verifie, actif, created_at)
 VALUES ('Nexora', 'Demo', 'demo-pro@nexora.sn', '+221770000000',
         'x$demo-not-a-real-hash-compte-vitrine-nexora-000000000000000', TRUE, TRUE, TRUE, NOW())
ON CONFLICT (email) DO NOTHING;

-- ---------- Compte administrateur de demonstration -------------------
--  Identifiants : admin@nexora.sn / admin123  (hachage BCrypt).
--  Profil ADMIN (seede par seed.sql) -> acces a l'administration.
INSERT INTO utilisateur (nom, prenom, email, telephone, mot_de_passe, statut_compte, email_verifie, actif, created_at, id_profile)
 VALUES ('Admin', 'Nexora', 'admin@nexora.sn', '+221770000001',
         '$2a$12$pfD3F0OGuHHFAxmgCaG26.t3L5PTqR9GXUdN.1SW4LyXDhjiuB73O', TRUE, TRUE, TRUE, NOW(),
         (SELECT id_profile FROM profile WHERE libelle = 'ADMIN'))
ON CONFLICT (email) DO NOTHING;

-- ---------- Espaces professionnels -----------------------------------
INSERT INTO espace_professionnel
  (nom_commercial, slug, description, telephone_principal, verifie, certifie, etat, note_moyenne, nombre_avis,
   id_proprietaire, id_type_espace, actif, created_at)
SELECT v.nom, v.slug, v.descr, v.tel, TRUE, v.cert::boolean, TRUE, v.note::double precision, v.avis::integer,
       (SELECT id_utilisateur FROM utilisateur WHERE email='demo-pro@nexora.sn'),
       (SELECT id_type_espace FROM type_espace_professionnel WHERE code=v.tcode),
       TRUE, NOW()
FROM (VALUES
  ('Le Baobab Gourmand', 'le-baobab-gourmand', 'Cuisine senegalaise chaleureuse au coeur du Plateau : thieboudienne, yassa et jus de bissap maison.', '+221338000000', TRUE,  4.8, 214, 'RESTAURANT'),
  ('Hotel Teranga',      'hotel-teranga',      'Hotel 4 etoiles avec piscine et vue mer, a deux pas du centre-ville.',                                   '+221338000001', TRUE,  4.6, 180, 'HOTEL'),
  ('DepannPro Plomberie','depannpro-plomberie','Interventions plomberie 24/7 : fuites, installations sanitaires, debouchage.',                            '+221770000010', FALSE, 4.9,  96, 'ATELIER'),
  ('Casino Supermarche', 'casino-supermarche', 'Supermarche de proximite : produits frais, epicerie et rayons du quotidien.',                            '+221338000002', TRUE,  4.3, 320, 'SUPERMARCHE'),
  ('Pharmacie du Plateau','pharmacie-du-plateau','Pharmacie ouverte 7j/7, garde de nuit et conseil officinal.',                                          '+221338000003', TRUE,  4.7, 142, 'PHARMACIE'),
  ('Yobante Transport',  'yobante-transport',  'Course urbaine et livraison rapide dans le grand Dakar.',                                                '+221770000011', TRUE,  4.5,  77, 'TRANSPORT')
) AS v(nom, slug, descr, tel, cert, note, avis, tcode)
WHERE NOT EXISTS (SELECT 1 FROM espace_professionnel e WHERE e.slug = v.slug);

-- ---------- Adresses (Dakar / Plateau) -------------------------------
INSERT INTO adresse (pays, region, ville, quartier, latitude, longitude, id_espace)
SELECT 'Senegal', 'Dakar', 'Dakar', v.quartier, v.lat::double precision, v.lon::double precision,
       (SELECT id_espace FROM espace_professionnel WHERE slug = v.slug)
FROM (VALUES
  ('le-baobab-gourmand',   'Plateau',   14.6720, -17.4290),
  ('hotel-teranga',        'Corniche',  14.6650, -17.4380),
  ('depannpro-plomberie',  'Medina',    14.6810, -17.4510),
  ('casino-supermarche',   'Plateau',   14.6745, -17.4315),
  ('pharmacie-du-plateau', 'Plateau',   14.6702, -17.4338),
  ('yobante-transport',    'Point E',   14.6905, -17.4620)
) AS v(slug, quartier, lat, lon)
WHERE (SELECT id_espace FROM espace_professionnel WHERE slug = v.slug) IS NOT NULL
  AND NOT EXISTS (
        SELECT 1 FROM adresse a
         WHERE a.id_espace = (SELECT id_espace FROM espace_professionnel WHERE slug = v.slug));

-- ---------- Offres PUBLIEES ------------------------------------------
INSERT INTO offre (type_offre_dtype, titre, description, prix, disponible, statut, date_creation,
                   id_espace, id_categorie, id_type_offre, type_service, service_disponible, actif, created_at)
SELECT v.dtype, v.titre, v.descr, v.prix, TRUE, 'PUBLIEE', NOW(),
       (SELECT id_espace    FROM espace_professionnel WHERE slug = v.eslug),
       (SELECT id_categorie FROM categorie_offre      WHERE slug = v.cslug),
       (SELECT id_type_offre FROM type_offre          WHERE libelle = v.tlib),
       v.tservice, TRUE, TRUE, NOW()
FROM (VALUES
  ('OFFRE',   'Le Baobab Gourmand',    'Table conviviale, cuisine senegalaise, terrasse ouverte le soir.', 7500::numeric,  'le-baobab-gourmand',   'restaurant',  'SERVICE', NULL),
  ('OFFRE',   'Hotel Teranga',         'Chambre double avec petit-dejeuner, piscine et wifi.',            45000::numeric,  'hotel-teranga',        'hotels',      'SERVICE', NULL),
  ('SERVICE', 'DepannPro Plomberie',   'Depannage plomberie a domicile, devis gratuit.',                   5000::numeric,  'depannpro-plomberie',  'plomberie',   'SERVICE', 'Depannage'),
  ('OFFRE',   'Casino Supermarche',    'Vos courses du quotidien, produits frais et epicerie.',              NULL::numeric, 'casino-supermarche',   'supermarche', 'PRODUIT', NULL),
  ('OFFRE',   'Pharmacie du Plateau',  'Medicaments, parapharmacie et conseil, ouvert 7j/7.',                NULL::numeric, 'pharmacie-du-plateau', 'pharmacie',   'PRODUIT', NULL),
  ('SERVICE', 'Yobante Transport',     'Course urbaine et livraison rapide dans Dakar.',                    1500::numeric,  'yobante-transport',    'transport',   'SERVICE', 'Course urbaine')
) AS v(dtype, titre, descr, prix, eslug, cslug, tlib, tservice)
WHERE (SELECT id_espace FROM espace_professionnel WHERE slug = v.eslug) IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM offre o WHERE o.titre = v.titre);

-- ---------- Photos (image principale + galerie) ----------------------
--  La 1re image (ordre 0, principale) alimente la carte de resultat ;
--  les suivantes alimentent la galerie de la fiche detail.
INSERT INTO image (url, principale, alt, ordre, id_offre)
SELECT v.url, v.principale::boolean, v.alt, v.ordre::integer,
       (SELECT id_offre FROM offre WHERE titre = v.titre ORDER BY id_offre LIMIT 1)
FROM (VALUES
  -- Le Baobab Gourmand (galerie)
  ('Le Baobab Gourmand',   'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1200&q=75&fm=jpg&fit=crop&auto=format', TRUE,  'Salle du restaurant', 0),
  ('Le Baobab Gourmand',   'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=1200&q=75&fm=jpg&fit=crop&auto=format',  FALSE, 'Assiette equilibree', 1),
  ('Le Baobab Gourmand',   'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=1200&q=75&fm=jpg&fit=crop&auto=format', FALSE, 'Plat dresse',         2),
  -- Hotel Teranga
  ('Hotel Teranga',        'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200&q=75&fm=jpg&fit=crop&auto=format', TRUE,  'Hotel et piscine',    0),
  -- DepannPro Plomberie
  ('DepannPro Plomberie',  'https://images.unsplash.com/photo-1607472586893-edb57bdc0e39?w=1200&q=75&fm=jpg&fit=crop&auto=format', TRUE,  'Installation sanitaire', 0),
  -- Casino Supermarche (galerie)
  ('Casino Supermarche',   'https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200&q=75&fm=jpg&fit=crop&auto=format',  TRUE,  'Rayons du supermarche', 0),
  ('Casino Supermarche',   'https://images.unsplash.com/photo-1578916171728-46686eac8d58?w=1200&q=75&fm=jpg&fit=crop&auto=format', FALSE, 'Fruits et legumes',     1),
  -- Pharmacie du Plateau
  ('Pharmacie du Plateau', 'https://images.unsplash.com/photo-1587854692152-cbe660dbde88?w=1200&q=75&fm=jpg&fit=crop&auto=format', TRUE,  'Medicaments',          0),
  -- Yobante Transport
  ('Yobante Transport',    'https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=1200&q=75&fm=jpg&fit=crop&auto=format', TRUE,  'Course urbaine',       0)
) AS v(titre, url, principale, alt, ordre)
WHERE (SELECT id_offre FROM offre WHERE titre = v.titre LIMIT 1) IS NOT NULL
  AND NOT EXISTS (
        SELECT 1 FROM image i
         WHERE i.id_offre = (SELECT id_offre FROM offre WHERE titre = v.titre ORDER BY id_offre LIMIT 1)
           AND i.url = v.url);

COMMIT;

-- =====================================================================
--  Verification rapide (facultatif) :
--    SELECT o.titre, i.url FROM offre o
--      JOIN image i ON i.id_offre = o.id_offre
--     WHERE o.statut = 'PUBLIEE' ORDER BY o.titre, i.ordre;
-- =====================================================================
