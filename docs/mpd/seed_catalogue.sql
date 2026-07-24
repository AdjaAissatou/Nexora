-- =====================================================================
--  NEXORA - Referentiel & catalogue enrichis (listes deroulantes)
--  A executer APRES schema.sql et seed.sql.
--  Objectif : que les formulaires soient quasi 100% en listes deroulantes.
--  Idempotent (ON CONFLICT / NOT EXISTS) : re-executable sans erreur.
-- =====================================================================

-- ============================ DEVISES ================================
INSERT INTO devise (code, libelle, symbole) VALUES
 ('XOF','Franc CFA (BCEAO)','CFA'),
 ('EUR','Euro','EUR'),
 ('USD','Dollar americain','$'),
 ('GBP','Livre sterling','GBP'),
 ('MAD','Dirham marocain','MAD'),
 ('NGN','Naira','NGN'),
 ('GHS','Cedi','GHS')
ON CONFLICT (code) DO NOTHING;

-- ============================ PAYS ===================================
INSERT INTO pays (code, nom, indicatif) VALUES
 ('SN','Senegal','+221'), ('ML','Mali','+223'), ('CI','Cote d''Ivoire','+225'),
 ('GN','Guinee','+224'), ('MR','Mauritanie','+222'), ('GM','Gambie','+220'),
 ('BF','Burkina Faso','+226'), ('FR','France','+33')
ON CONFLICT (code) DO NOTHING;

-- ===================== REGIONS DU SENEGAL ============================
INSERT INTO region (nom, id_pays)
 SELECT r.nom, p.id_pays FROM pays p
 JOIN (VALUES ('Dakar'),('Thies'),('Diourbel'),('Fatick'),('Kaolack'),
              ('Kaffrine'),('Kolda'),('Sedhiou'),('Ziguinchor'),('Tambacounda'),
              ('Kedougou'),('Louga'),('Matam'),('Saint-Louis')) AS r(nom) ON TRUE
 WHERE p.code='SN'
   AND NOT EXISTS (SELECT 1 FROM region x WHERE x.nom=r.nom AND x.id_pays=p.id_pays);

-- ===================== VILLES / COMMUNES =============================
-- Dakar
INSERT INTO ville (nom, id_region)
 SELECT v.nom, r.id_region FROM region r
 JOIN (VALUES ('Dakar-Plateau'),('Grand Dakar'),('Parcelles Assainies'),
              ('Guediawaye'),('Pikine'),('Rufisque'),('Yoff'),('Ngor'),
              ('Ouakam'),('Almadies'),('Medina'),('Point E'),('Mermoz'),
              ('Sacre-Coeur'),('Liberte'),('Keur Massar'),('Diamniadio')) AS v(nom) ON TRUE
 WHERE r.nom='Dakar'
   AND NOT EXISTS (SELECT 1 FROM ville x WHERE x.nom=v.nom AND x.id_region=r.id_region);
-- Thies
INSERT INTO ville (nom, id_region)
 SELECT v.nom, r.id_region FROM region r
 JOIN (VALUES ('Thies'),('Mbour'),('Tivaouane'),('Saly'),('Joal-Fadiouth'),('Pout')) AS v(nom) ON TRUE
 WHERE r.nom='Thies'
   AND NOT EXISTS (SELECT 1 FROM ville x WHERE x.nom=v.nom AND x.id_region=r.id_region);
-- Autres regions (chefs-lieux + villes principales)
INSERT INTO ville (nom, id_region)
 SELECT v.nom, r.id_region FROM region r
 JOIN (VALUES ('Saint-Louis','Saint-Louis'),('Richard-Toll','Saint-Louis'),
              ('Kaolack','Kaolack'),('Guinguineo','Kaolack'),
              ('Ziguinchor','Ziguinchor'),('Bignona','Ziguinchor'),
              ('Diourbel','Diourbel'),('Touba','Diourbel'),('Mbacke','Diourbel'),
              ('Louga','Louga'),('Kebemer','Louga'),
              ('Tambacounda','Tambacounda'),('Bakel','Tambacounda'),
              ('Kolda','Kolda'),('Velingara','Kolda'),
              ('Fatick','Fatick'),('Foundiougne','Fatick'),
              ('Matam','Matam'),('Ourossogui','Matam'),
              ('Kaffrine','Kaffrine'),('Sedhiou','Sedhiou'),('Kedougou','Kedougou')
      ) AS v(nom, region) ON r.nom = v.region
   AND NOT EXISTS (SELECT 1 FROM ville x WHERE x.nom=v.nom AND x.id_region=r.id_region);

-- ===================== MODES DE PAIEMENT ============================
INSERT INTO mode_paiement (libelle) VALUES
 ('Wave'), ('Orange Money'), ('Free Money'), ('Carte bancaire'),
 ('PayPal'), ('Virement bancaire'), ('Paiement a la livraison')
ON CONFLICT (libelle) DO NOTHING;

-- ===================== TYPES D'ESPACE (complement) ==================
INSERT INTO type_espace_professionnel (code, libelle) VALUES
 ('MAGASIN','Magasin'), ('ENTREPRISE','Entreprise'), ('BUREAU','Bureau'),
 ('USINE','Usine'), ('ECOLE','Ecole'), ('UNIVERSITE','Universite'),
 ('HOTEL','Hotel'), ('FERME','Ferme'), ('SALON','Salon de coiffure'),
 ('BOULANGERIE','Boulangerie'), ('LIBRAIRIE','Librairie'),
 ('LABORATOIRE','Laboratoire'), ('BANQUE','Banque'), ('ASSURANCE','Assurance'),
 ('ADMINISTRATION','Administration'), ('ONG','ONG'), ('ASSOCIATION','Association'),
 ('COWORKING','Espace de coworking'), ('SALLE_EVENEMENT','Salle d''evenement'),
 ('STATION','Station-service'), ('CENTRE_FORMATION','Centre de formation'),
 ('INSTITUT_BEAUTE','Institut de beaute'), ('SALLE_SPORT','Salle de sport')
ON CONFLICT (code) DO NOTHING;

-- ===================== TYPES DE CATEGORIE (complement) =============
INSERT INTO type_categorie (code, libelle) VALUES
 ('MODE','Mode & Vetements'), ('ALIMENTAIRE','Alimentaire'), ('BEAUTE','Beaute & Bien-etre'),
 ('EDUCATION','Education'), ('JURIDIQUE','Juridique'), ('FINANCE','Finance'),
 ('INFORMATIQUE','Informatique & Digital'), ('EVENEMENTIEL','Evenementiel'),
 ('AGRICULTURE','Agriculture'), ('MAISON','Maison & Deco'), ('SPORT','Sport & Loisirs'),
 ('TOURISME','Tourisme & Hebergement')
ON CONFLICT (code) DO NOTHING;

-- =====================================================================
--  CATEGORIES + ATTRIBUTS DYNAMIQUES (valeurs = listes deroulantes)
--  Motif reutilisable :
--   1) creer la categorie (slug unique)
--   2) creer l'attribut (LIST) si absent
--   3) inserer ses valeurs possibles
-- =====================================================================

-- ---------- helper implicite : type_champ LIST/NUMBER/BOOLEAN existent (seed.sql) ----------

-- ============ MODE : Vetements ============
INSERT INTO categorie_offre (nom, slug, niveau, populaire, id_type_categorie)
 VALUES ('Vetements','vetements',0,TRUE,(SELECT id_type_categorie FROM type_categorie WHERE code='MODE'))
ON CONFLICT (slug) DO NOTHING;

INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT x.nom, x.obl, TRUE, c.id_categorie, t.id_type_champ
 FROM categorie_offre c, type_champ t,
   (VALUES ('Genre',TRUE),('Taille',TRUE),('Couleur',TRUE),('Matiere',FALSE),('Etat',TRUE)) AS x(nom,obl)
 WHERE c.slug='vetements' AND t.code='LIST'
   AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.nom=x.nom AND a.id_categorie=c.id_categorie);

INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Homme',1),('Femme',2),('Enfant',3),('Mixte',4)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Genre' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='vetements')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('XS',1),('S',2),('M',3),('L',4),('XL',5),('XXL',6),('3XL',7)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Taille' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='vetements')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Blanc',1),('Noir',2),('Rouge',3),('Bleu',4),('Vert',5),('Jaune',6),
              ('Gris',7),('Rose',8),('Marron',9),('Multicolore',10)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Couleur' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='vetements')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Coton',1),('Laine',2),('Cuir',3),('Jean',4),('Soie',5),('Lin',6),('Polyester',7)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Matiere' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='vetements')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Neuf',1),('Occasion',2)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Etat' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='vetements')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);

-- ============ MODE : Chaussures ============
INSERT INTO categorie_offre (nom, slug, niveau, id_type_categorie)
 VALUES ('Chaussures','chaussures',0,(SELECT id_type_categorie FROM type_categorie WHERE code='MODE'))
ON CONFLICT (slug) DO NOTHING;
INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT x.nom, TRUE, TRUE, c.id_categorie, t.id_type_champ
 FROM categorie_offre c, type_champ t, (VALUES ('Pointure'),('Genre'),('Couleur'),('Etat')) AS x(nom)
 WHERE c.slug='chaussures' AND t.code='LIST'
   AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.nom=x.nom AND a.id_categorie=c.id_categorie);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('35',1),('36',2),('37',3),('38',4),('39',5),('40',6),('41',7),
              ('42',8),('43',9),('44',10),('45',11),('46',12)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Pointure' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='chaussures')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);

-- ============ ELECTRONIQUE : Telephones ============
INSERT INTO categorie_offre (nom, slug, niveau, populaire, id_type_categorie)
 VALUES ('Telephones','telephones',0,TRUE,(SELECT id_type_categorie FROM type_categorie WHERE code='ELECTRONIQUE'))
ON CONFLICT (slug) DO NOTHING;
INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT x.nom, x.obl, TRUE, c.id_categorie, t.id_type_champ
 FROM categorie_offre c, type_champ t,
   (VALUES ('Marque',TRUE),('Stockage',TRUE),('Memoire RAM',FALSE),('Etat',TRUE),('Couleur',FALSE)) AS x(nom,obl)
 WHERE c.slug='telephones' AND t.code='LIST'
   AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.nom=x.nom AND a.id_categorie=c.id_categorie);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Samsung',1),('Apple',2),('Xiaomi',3),('Tecno',4),('Infinix',5),
              ('Huawei',6),('Itel',7),('Oppo',8),('Nokia',9),('Google',10)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Marque' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='telephones')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('16 Go',1),('32 Go',2),('64 Go',3),('128 Go',4),('256 Go',5),('512 Go',6),('1 To',7)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Stockage' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='telephones')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Neuf',1),('Comme neuf',2),('Occasion',3),('Reconditionne',4)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Etat' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='telephones')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);

-- ============ AUTOMOBILE : Voitures ============
INSERT INTO categorie_offre (nom, slug, niveau, populaire, id_type_categorie)
 VALUES ('Voitures','voitures',0,TRUE,(SELECT id_type_categorie FROM type_categorie WHERE code='AUTOMOBILE'))
ON CONFLICT (slug) DO NOTHING;
INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT x.nom, x.obl, TRUE, c.id_categorie, t.id_type_champ
 FROM categorie_offre c, type_champ t,
   (VALUES ('Marque',TRUE),('Carburant',TRUE),('Boite de vitesses',TRUE),('Etat',TRUE),('Couleur',FALSE)) AS x(nom,obl)
 WHERE c.slug='voitures' AND t.code='LIST'
   AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.nom=x.nom AND a.id_categorie=c.id_categorie);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Toyota',1),('Mercedes',2),('Hyundai',3),('Kia',4),('Peugeot',5),('Renault',6),
              ('Nissan',7),('Ford',8),('BMW',9),('Volkswagen',10),('Audi',11),('Honda',12)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Marque' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='voitures')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Essence',1),('Diesel',2),('Hybride',3),('Electrique',4),('GPL',5)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Carburant' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='voitures')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Manuelle',1),('Automatique',2)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Boite de vitesses' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='voitures')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);

-- ============ IMMOBILIER : Location & Vente ============
INSERT INTO categorie_offre (nom, slug, niveau, populaire, id_type_categorie)
 VALUES ('Immobilier','immobilier',0,TRUE,(SELECT id_type_categorie FROM type_categorie WHERE code='IMMOBILIER'))
ON CONFLICT (slug) DO NOTHING;
INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT x.nom, x.obl, TRUE, c.id_categorie, t.id_type_champ
 FROM categorie_offre c, type_champ t,
   (VALUES ('Type de bien',TRUE),('Transaction',TRUE),('Chambres',FALSE),('Standing',FALSE),('Meuble',FALSE)) AS x(nom,obl)
 WHERE c.slug='immobilier' AND t.code='LIST'
   AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.nom=x.nom AND a.id_categorie=c.id_categorie);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Appartement',1),('Maison',2),('Villa',3),('Terrain',4),('Bureau',5),
              ('Magasin',6),('Chambre',7),('Studio',8)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Type de bien' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='immobilier')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Location',1),('Vente',2),('Location courte duree',3)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Transaction' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='immobilier')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('1',1),('2',2),('3',3),('4',4),('5',5),('6+',6)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Chambres' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='immobilier')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);

-- ============ RESTAURATION : Restaurant (service) ============
INSERT INTO categorie_offre (nom, slug, niveau, populaire, id_type_categorie)
 VALUES ('Restaurant','restaurant',0,TRUE,(SELECT id_type_categorie FROM type_categorie WHERE code='RESTAURATION'))
ON CONFLICT (slug) DO NOTHING;
INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT 'Type de cuisine', TRUE, TRUE, c.id_categorie, t.id_type_champ
 FROM categorie_offre c, type_champ t WHERE c.slug='restaurant' AND t.code='LIST'
   AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.nom='Type de cuisine' AND a.id_categorie=c.id_categorie);
INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT 'Options', FALSE, TRUE, c.id_categorie, t.id_type_champ
 FROM categorie_offre c, type_champ t WHERE c.slug='restaurant' AND t.code='MULTI_LIST'
   AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.nom='Options' AND a.id_categorie=c.id_categorie);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Senegalaise',1),('Africaine',2),('Europeenne',3),('Asiatique',4),
              ('Libanaise',5),('Fast-food',6),('Pizzeria',7),('Grillades',8),('Patisserie',9)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Type de cuisine' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='restaurant')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Terrasse',1),('Livraison',2),('Wi-Fi',3),('Climatisation',4),
              ('Halal',5),('Vegetarien',6),('Parking',7)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Options' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='restaurant')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);

-- ============ TOURISME : Hotel (espace) ============
INSERT INTO categorie_offre (nom, slug, niveau, populaire, id_type_categorie)
 VALUES ('Hotel','hotel',0,TRUE,(SELECT id_type_categorie FROM type_categorie WHERE code='TOURISME'))
ON CONFLICT (slug) DO NOTHING;
INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT 'Categorie (etoiles)', TRUE, TRUE, c.id_categorie, t.id_type_champ
 FROM categorie_offre c, type_champ t WHERE c.slug='hotel' AND t.code='LIST'
   AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.nom='Categorie (etoiles)' AND a.id_categorie=c.id_categorie);
INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT 'Equipements', FALSE, TRUE, c.id_categorie, t.id_type_champ
 FROM categorie_offre c, type_champ t WHERE c.slug='hotel' AND t.code='MULTI_LIST'
   AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.nom='Equipements' AND a.id_categorie=c.id_categorie);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('1 etoile',1),('2 etoiles',2),('3 etoiles',3),('4 etoiles',4),('5 etoiles',5)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Categorie (etoiles)' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='hotel')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Piscine',1),('Parking',2),('Climatisation',3),('Wi-Fi',4),
              ('Petit-dejeuner',5),('Salle de sport',6),('Restaurant',7),('Navette',8)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Equipements' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='hotel')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);

-- ============ SANTE : Medecin (service) ============
INSERT INTO categorie_offre (nom, slug, niveau, populaire, id_type_categorie)
 VALUES ('Medecin','medecin',0,TRUE,(SELECT id_type_categorie FROM type_categorie WHERE code='SANTE'))
ON CONFLICT (slug) DO NOTHING;
INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT x.nom, x.obl, TRUE, c.id_categorie, t.id_type_champ
 FROM categorie_offre c, type_champ t,
   (VALUES ('Specialite',TRUE),('Mode de consultation',TRUE)) AS x(nom,obl)
 WHERE c.slug='medecin' AND t.code='LIST'
   AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.nom=x.nom AND a.id_categorie=c.id_categorie);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Generaliste',1),('Dentiste',2),('Cardiologue',3),('Pediatre',4),
              ('Gynecologue',5),('Dermatologue',6),('Ophtalmologue',7),('ORL',8),
              ('Kinesitherapeute',9),('Psychologue',10)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Specialite' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='medecin')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Au cabinet',1),('A domicile',2),('En ligne',3)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Mode de consultation' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='medecin')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);

-- ============ BEAUTE : Coiffure & Beaute (service) ============
INSERT INTO categorie_offre (nom, slug, niveau, id_type_categorie)
 VALUES ('Coiffure & Beaute','coiffure-beaute',0,(SELECT id_type_categorie FROM type_categorie WHERE code='BEAUTE'))
ON CONFLICT (slug) DO NOTHING;
INSERT INTO attribut (nom, obligatoire, filtrable, id_categorie, id_type_champ)
 SELECT x.nom, TRUE, TRUE, c.id_categorie, t.id_type_champ
 FROM categorie_offre c, type_champ t, (VALUES ('Prestation'),('Genre')) AS x(nom)
 WHERE c.slug='coiffure-beaute' AND t.code='LIST'
   AND NOT EXISTS (SELECT 1 FROM attribut a WHERE a.nom=x.nom AND a.id_categorie=c.id_categorie);
INSERT INTO valeur_attribut_possible (valeur, ordre, id_attribut)
 SELECT v.valeur, v.ordre, a.id_attribut FROM attribut a
 JOIN (VALUES ('Coupe',1),('Coloration',2),('Tresses',3),('Barbe',4),('Manucure',5),
              ('Maquillage',6),('Soins visage',7),('Massage',8)) AS v(valeur,ordre) ON TRUE
 WHERE a.nom='Prestation' AND a.id_categorie=(SELECT id_categorie FROM categorie_offre WHERE slug='coiffure-beaute')
   AND NOT EXISTS (SELECT 1 FROM valeur_attribut_possible w WHERE w.id_attribut=a.id_attribut AND w.valeur=v.valeur);

-- ============ Categories additionnelles (sans attributs specifiques) ============
INSERT INTO categorie_offre (nom, slug, niveau, id_type_categorie) VALUES
 ('Ordinateurs de bureau','ordinateurs-bureau',0,(SELECT id_type_categorie FROM type_categorie WHERE code='ELECTRONIQUE')),
 ('Televiseurs','televiseurs',0,(SELECT id_type_categorie FROM type_categorie WHERE code='ELECTRONIQUE')),
 ('Electromenager','electromenager',0,(SELECT id_type_categorie FROM type_categorie WHERE code='MAISON')),
 ('Meubles','meubles',0,(SELECT id_type_categorie FROM type_categorie WHERE code='MAISON')),
 ('Motos','motos',0,(SELECT id_type_categorie FROM type_categorie WHERE code='AUTOMOBILE')),
 ('Pieces detachees','pieces-detachees',0,(SELECT id_type_categorie FROM type_categorie WHERE code='AUTOMOBILE')),
 ('Produits alimentaires','alimentaire',0,(SELECT id_type_categorie FROM type_categorie WHERE code='ALIMENTAIRE')),
 ('Cosmetiques','cosmetiques',0,(SELECT id_type_categorie FROM type_categorie WHERE code='BEAUTE')),
 ('Electricite','electricite',0,(SELECT id_type_categorie FROM type_categorie WHERE code='BATIMENT')),
 ('Menuiserie','menuiserie',0,(SELECT id_type_categorie FROM type_categorie WHERE code='BATIMENT')),
 ('Mecanique auto','mecanique',0,(SELECT id_type_categorie FROM type_categorie WHERE code='AUTOMOBILE')),
 ('Transport & Livraison','transport',0,(SELECT id_type_categorie FROM type_categorie WHERE code='TRANSPORT')),
 ('Avocat','avocat',0,(SELECT id_type_categorie FROM type_categorie WHERE code='JURIDIQUE')),
 ('Comptable','comptable',0,(SELECT id_type_categorie FROM type_categorie WHERE code='FINANCE')),
 ('Developpement web & mobile','developpement',0,(SELECT id_type_categorie FROM type_categorie WHERE code='INFORMATIQUE')),
 ('Design graphique','design',0,(SELECT id_type_categorie FROM type_categorie WHERE code='INFORMATIQUE')),
 ('Formation','formation',0,(SELECT id_type_categorie FROM type_categorie WHERE code='EDUCATION')),
 ('Salle d''evenement','salle-evenement',0,(SELECT id_type_categorie FROM type_categorie WHERE code='EVENEMENTIEL'))
ON CONFLICT (slug) DO NOTHING;

-- =====================================================================
--  Fin du referentiel enrichi.
-- =====================================================================
