-- Type_offre spécifiques + attributs dynamiques + valeurs prédéfinies + tags,
-- adaptés du fichier fourni par l'utilisateur (contenu conservé, génération
-- SQL et rattachement à la taxonomie réécrits : la fonction fournie utilisait
-- une colonne parent_id dupliquant id_categorie_parent, ON CONFLICT sur des
-- contraintes qui n'existent pas sur ce schéma (valeur_attribut_possible), et
-- ne gérait pas les catégories qui ont à la fois des sous-catégories et leurs
-- propres types d'offre (ex: Téléphones et tablettes a une sous-catégorie
-- Accessoires ET se vend elle-même directement) — corrigé côté formulaire dans
-- CreerOffreBean plutôt que contourné ici. Noms de domaines racines réalignés
-- sur ceux déjà en place (14_taxonomie_domaines.sql) pour ne pas créer de
-- domaines dupliqués. Idempotent.

CREATE OR REPLACE FUNCTION nexora_ensure_category_path(p_path TEXT) RETURNS BIGINT
LANGUAGE plpgsql AS $$
DECLARE
    v_parts TEXT[] := string_to_array(p_path, '/');
    v_parent BIGINT := NULL; v_id BIGINT; v_name TEXT; i INTEGER;
BEGIN
    FOR i IN 1..array_length(v_parts,1) LOOP
        v_name := trim(v_parts[i]);
        SELECT id_categorie INTO v_id FROM categorie
            WHERE nom = v_name AND id_categorie_parent IS NOT DISTINCT FROM v_parent;
        IF v_id IS NULL THEN
            INSERT INTO categorie(nom, id_categorie_parent, actif)
            VALUES (v_name, v_parent, TRUE)
            RETURNING id_categorie INTO v_id;
        END IF;
        v_parent := v_id;
    END LOOP;
    RETURN v_parent;
END; $$;

-- Types d'offre spécifiques (remplacent/complètent le nom générique de la
-- feuille posé par 15_type_offre_domaines.sql quand un libellé plus précis
-- est fourni ici).
DO $$
DECLARE v_cat BIGINT;
BEGIN
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements femme/Robes');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Robes') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Robes', 'Robes femme : casual, cérémonie, soirée, traditionnelle', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements femme/Jupes');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Jupes') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Jupes', 'Jupes et modèles féminins', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements femme/Pantalons');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Pantalons femme') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Pantalons femme', 'Pantalons et bas femme', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements femme/Ensembles');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Ensembles femme') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Ensembles femme', 'Ensembles coordonnés femme', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements femme/Boubous et grands boubous');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Boubous femme') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Boubous femme', 'Boubous, grands boubous et tenues traditionnelles', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements homme/Chemises');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Chemises homme') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Chemises homme', 'Chemises homme', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements homme/Pantalons');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Pantalons homme') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Pantalons homme', 'Pantalons homme', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements homme/Costumes');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Costumes homme') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Costumes homme', 'Costumes et ensembles formels', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements homme/Boubous');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Boubous homme') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Boubous homme', 'Boubous et tenues traditionnelles homme', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Enfant/Vêtements fille');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Vêtements fille') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Vêtements fille', 'Vêtements pour fille', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Enfant/Vêtements garçon');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Vêtements garçon') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Vêtements garçon', 'Vêtements pour garçon', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Enfant/Bébé');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Vêtements bébé') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Vêtements bébé', 'Vêtements et accessoires bébé', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Tissus/Wax');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Wax') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Wax', 'Tissus wax', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Tissus/Bazin');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Bazin') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Bazin', 'Bazin riche, bazin simple et variantes', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Tissus/Dentelle');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Dentelle') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Dentelle', 'Dentelles et tissus de cérémonie', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Tissus/Coton');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Coton') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Coton', 'Tissus coton', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Chaussures/Femme');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Chaussures femme') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Chaussures femme', 'Chaussures femme', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Chaussures/Homme');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Chaussures homme') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Chaussures homme', 'Chaussures homme', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Chaussures/Enfant');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Chaussures enfant') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Chaussures enfant', 'Chaussures enfant', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Accessoires/Sacs');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Sacs') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Sacs', 'Sacs et maroquinerie', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Accessoires/Bijoux');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Bijoux') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Bijoux', 'Bijoux et accessoires', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Mode et textile/Accessoires/Foulards');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Foulards') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Foulards', 'Foulards, voiles et accessoires de tête', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Beauté/Soins du visage');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Soins du visage') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Soins du visage', 'Produits et prestations de soins du visage', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Beauté/Soins du corps');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Soins du corps') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Soins du corps', 'Produits et prestations de soins du corps', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Beauté/Cheveux/Coiffure');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Coiffure') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Coiffure', 'Prestations de coiffure', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Beauté/Cheveux/Extensions et perruques');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Extensions et perruques') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Extensions et perruques', 'Extensions, mèches et perruques', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Beauté/Maquillage');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Maquillage') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Maquillage', 'Maquillage et prestations maquillage', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Beauté/Parfumerie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Parfums') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Parfums', 'Parfumerie et fragrances', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Beauté/Onglerie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Onglerie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Onglerie', 'Manucure, pédicure et pose d''ongles', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Beauté/Massage et spa');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Massage et spa') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Massage et spa', 'Massages, spa et bien-être', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Téléphones et tablettes');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Téléphones et smartphones') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Téléphones et smartphones', 'Smartphones et téléphones mobiles', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Téléphones et tablettes/Accessoires');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Accessoires téléphone') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Accessoires téléphone', 'Coques, chargeurs, câbles, protections', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Informatique/Ordinateurs portables');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Ordinateurs portables') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Ordinateurs portables', 'PC portables', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Informatique/Ordinateurs de bureau');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Ordinateurs de bureau') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Ordinateurs de bureau', 'PC fixes et stations de travail', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Informatique/Composants');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Composants informatiques') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Composants informatiques', 'RAM, SSD, processeurs, cartes graphiques, etc.', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Réseaux');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Équipements réseau') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Équipements réseau', 'Routeurs, switches, points d''accès, câblage', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Services numériques/Développement logiciel');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Développement logiciel') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Développement logiciel', 'Développement d''applications et logiciels', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Services numériques/Développement web');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Développement web') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Développement web', 'Sites et applications web', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Services numériques/Développement mobile');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Développement mobile') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Développement mobile', 'Applications Android et iOS', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Services numériques/Design graphique');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Design graphique') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Design graphique', 'Création graphique et identité visuelle', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Services numériques/Maintenance informatique');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Maintenance informatique') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Maintenance informatique', 'Installation, dépannage et maintenance', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Services numériques/Cybersécurité');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Cybersécurité') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Cybersécurité', 'Audit, sécurisation et protection des systèmes', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Informatique/Services numériques/Formation informatique');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Formation informatique') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Formation informatique', 'Cours et formations numériques', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Commerce/Alimentation/Épicerie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Produits d''épicerie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Produits d''épicerie', 'Produits alimentaires courants', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Commerce/Alimentation/Riz et céréales');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Riz et céréales') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Riz et céréales', 'Riz, mil, maïs, sorgho et autres céréales', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Commerce/Alimentation/Fruits et légumes');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Fruits et légumes') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Fruits et légumes', 'Produits frais végétaux', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Commerce/Alimentation/Viandes');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Viandes') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Viandes', 'Viandes fraîches et préparées', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Commerce/Alimentation/Poissons et fruits de mer');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Poissons et fruits de mer') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Poissons et fruits de mer', 'Produits de la mer et de la pêche', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Commerce/Boissons');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Boissons') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Boissons', 'Eaux, jus et boissons diverses', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Commerce/Maison');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Articles maison') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Articles maison', 'Articles et équipements domestiques', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Commerce/Électronique');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Électronique') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Électronique', 'Produits électroniques', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Commerce/Électroménager');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Électroménager') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Électroménager', 'Appareils électroménagers', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures céréalières/Riz');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Riz') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Riz', 'Riz produit ou commercialisé', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures céréalières/Mil');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Mil') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Mil', 'Mil', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures céréalières/Maïs');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Maïs') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Maïs', 'Maïs', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures céréalières/Sorgho');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Sorgho') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Sorgho', 'Sorgho', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures maraîchères/Oignon');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Oignon') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Oignon', 'Oignon', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures maraîchères/Pomme de terre');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Pomme de terre') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Pomme de terre', 'Pomme de terre', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures maraîchères/Tomate');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Tomate') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Tomate', 'Tomate', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures fruitières/Mangue');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Mangue') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Mangue', 'Mangue', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Élevage/Bovins');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Bovins') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Bovins', 'Élevage et vente de bovins', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Élevage/Ovins et caprins');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Ovins et caprins') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Ovins et caprins', 'Moutons, chèvres et produits associés', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Élevage/Volaille');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Volaille') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Volaille', 'Poulets et volailles', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Produits agricoles transformés');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Produits agricoles transformés') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Produits agricoles transformés', 'Produits issus de la transformation agricole', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Pêche et aquaculture/Poissons');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Poissons frais') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Poissons frais', 'Poissons frais issus de la pêche ou aquaculture', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Pêche et aquaculture/Fruits de mer');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Fruits de mer') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Fruits de mer', 'Crustacés, mollusques et autres fruits de mer', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Pêche et aquaculture/Produits transformés');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Produits halieutiques transformés') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Produits halieutiques transformés', 'Produits séchés, fumés, salés ou transformés', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agroalimentaire/Produits céréaliers');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Produits céréaliers transformés') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Produits céréaliers transformés', 'Farines, couscous, céréales transformées', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agroalimentaire/Boulangerie et pâtisserie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Boulangerie et pâtisserie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Boulangerie et pâtisserie', 'Pains, viennoiseries et pâtisseries', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agroalimentaire/Produits laitiers');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Produits laitiers') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Produits laitiers', 'Lait, yaourt, fromage et dérivés', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Agroalimentaire/Conserves et produits transformés');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Conserves et produits transformés') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Conserves et produits transformés', 'Produits alimentaires transformés et conserves', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Maison et habitat/Mobilier');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Mobilier') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Mobilier', 'Meubles et mobilier', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Maison et habitat/Décoration');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Décoration') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Décoration', 'Objets et accessoires de décoration', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Maison et habitat/Cuisine');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Articles de cuisine') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Articles de cuisine', 'Ustensiles et équipements de cuisine', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Maison et habitat/Literie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Literie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Literie', 'Matelas, oreillers, draps et literie', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Maison et habitat/Éclairage');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Éclairage') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Éclairage', 'Lampes, luminaires et équipements d''éclairage', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Maison et habitat/Entretien');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Produits d''entretien') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Produits d''entretien', 'Produits et équipements d''entretien', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Bâtiment et construction/Plomberie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Plomberie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Plomberie', 'Travaux et équipements de plomberie', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Bâtiment et construction/Électricité');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Électricité bâtiment') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Électricité bâtiment', 'Installation et travaux électriques', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Bâtiment et construction/Peinture');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Peinture') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Peinture', 'Travaux et prestations de peinture', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Bâtiment et construction/Maçonnerie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Maçonnerie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Maçonnerie', 'Travaux de maçonnerie', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Bâtiment et construction/Menuiserie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Menuiserie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Menuiserie', 'Menuiserie bois, aluminium ou PVC', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Bâtiment et construction/Climatisation');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Climatisation') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Climatisation', 'Installation et maintenance climatisation', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Bâtiment et construction/Matériaux');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Matériaux de construction') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Matériaux de construction', 'Ciment, fer, briques et autres matériaux', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Automobile/Entretien automobile/Vidange');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Vidange') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Vidange', 'Vidange et remplacement des fluides', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Automobile/Entretien automobile/Freinage');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Freinage') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Freinage', 'Entretien et réparation des freins', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Automobile/Entretien automobile/Pneumatiques');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Pneumatiques') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Pneumatiques', 'Pneus, montage et équilibrage', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Automobile/Mécanique/Moteur');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Réparation moteur') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Réparation moteur', 'Diagnostic et réparation moteur', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Automobile/Mécanique/Embrayage');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Embrayage') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Embrayage', 'Entretien et réparation embrayage', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Automobile/Électricité automobile');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Électricité automobile') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Électricité automobile', 'Diagnostic et réparation électrique automobile', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Automobile/Carrosserie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Carrosserie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Carrosserie', 'Réparation et peinture carrosserie', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Automobile/Diagnostic automobile');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Diagnostic automobile') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Diagnostic automobile', 'Diagnostic électronique et mécanique', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Automobile/Vente de véhicules');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Véhicules') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Véhicules', 'Vente de voitures, motos et autres véhicules', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Automobile/Pièces automobiles');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Pièces automobiles') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Pièces automobiles', 'Pièces détachées et accessoires', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Restauration/Cuisine sénégalaise/Plats');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Plats sénégalais') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Plats sénégalais', 'Plats traditionnels sénégalais', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Restauration/Cuisine africaine');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Plats africains') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Plats africains', 'Cuisine africaine', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Restauration/Fast-food');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Fast-food') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Fast-food', 'Burgers, sandwichs et restauration rapide', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Restauration/Pizzeria');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Pizza') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Pizza', 'Pizzas', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Restauration/Café et boissons');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Café et boissons') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Café et boissons', 'Cafés, thés et boissons', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Restauration/Pâtisserie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Pâtisserie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Pâtisserie', 'Pâtisseries et desserts', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Restauration/Traiteur');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Traiteur') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Traiteur', 'Prestations de traiteur', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Restauration/Livraison de repas');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Livraison de repas') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Livraison de repas', 'Livraison de repas et menus', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Hôtellerie/Hébergement/Chambres');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Chambres') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Chambres', 'Chambres et hébergement', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Hôtellerie/Hébergement/Suites');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Suites') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Suites', 'Suites et hébergement premium', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Restauration/Événementiel');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Salle et événementiel') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Salle et événementiel', 'Espaces et prestations pour événements', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Santé/Médecine générale');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Consultation médecine générale') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Consultation médecine générale', 'Consultation médicale générale', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Santé/Spécialités/Cardiologie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Consultation cardiologie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Consultation cardiologie', 'Consultation en cardiologie', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Santé/Spécialités/Dermatologie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Consultation dermatologie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Consultation dermatologie', 'Consultation dermatologique', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Santé/Spécialités/Gynécologie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Consultation gynécologie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Consultation gynécologie', 'Consultation gynécologique', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Santé/Spécialités/Pédiatrie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Consultation pédiatrie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Consultation pédiatrie', 'Consultation pédiatrique', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Santé/Laboratoire');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Analyses médicales') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Analyses médicales', 'Analyses et examens de laboratoire', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Santé/Imagerie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Imagerie médicale') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Imagerie médicale', 'Radiologie, échographie et imagerie', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Santé/Pharmacie/Médicaments');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Médicaments') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Médicaments', 'Médicaments et produits pharmaceutiques', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Santé/Pharmacie/Parapharmacie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Parapharmacie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Parapharmacie', 'Hygiène, soins et produits de santé', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Santé/Matériel médical');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Matériel médical') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Matériel médical', 'Équipements et dispositifs médicaux', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Éducation/Préscolaire');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Préscolaire') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Préscolaire', 'Éducation préscolaire', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Éducation/Primaire');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Enseignement primaire') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Enseignement primaire', 'Enseignement primaire', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Éducation/Secondaire');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Enseignement secondaire') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Enseignement secondaire', 'Collège et lycée', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Éducation/Formation professionnelle');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Formation professionnelle') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Formation professionnelle', 'Formations professionnelles', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Éducation/Informatique');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Formation informatique') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Formation informatique', 'Formations en informatique', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Éducation/Langues');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Cours de langues') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Cours de langues', 'Cours et formations linguistiques', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Éducation/Soutien scolaire');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Soutien scolaire') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Soutien scolaire', 'Cours de soutien et accompagnement', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Éducation/Université');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Formation universitaire') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Formation universitaire', 'Formations universitaires', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Immobilier/Vente immobilière');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Vente immobilière') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Vente immobilière', 'Vente de biens immobiliers', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Immobilier/Location/Appartements');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Location appartement') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Location appartement', 'Location d''appartements', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Immobilier/Location/Maisons');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Location maison') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Location maison', 'Location de maisons', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Immobilier/Terrains');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Terrains') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Terrains', 'Vente ou location de terrains', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Immobilier/Bureaux et locaux');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Bureaux et locaux') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Bureaux et locaux', 'Bureaux et locaux professionnels', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Immobilier/Gestion immobilière');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Gestion immobilière') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Gestion immobilière', 'Gestion et administration de biens', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Transport/Transport de personnes');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Transport de personnes') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Transport de personnes', 'Transport individuel ou collectif', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Transport/Livraison et coursier');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Livraison et coursier') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Livraison et coursier', 'Livraison de colis et courses', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Transport/Déménagement');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Déménagement') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Déménagement', 'Services de déménagement', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Transport/Transport de marchandises');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Transport de marchandises') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Transport de marchandises', 'Transport de marchandises', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Transport/Logistique');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Logistique') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Logistique', 'Stockage, distribution et logistique', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Conseil et services aux entreprises/Comptabilité');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Comptabilité') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Comptabilité', 'Prestations comptables', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Conseil et services aux entreprises/Audit');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Audit') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Audit', 'Audit et contrôle', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Conseil et services aux entreprises/Conseil');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Conseil') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Conseil', 'Conseil aux entreprises', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Conseil et services aux entreprises/Ressources humaines');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Ressources humaines') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Ressources humaines', 'Recrutement et conseil RH', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Conseil et services aux entreprises/Marketing');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Marketing') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Marketing', 'Marketing et stratégie commerciale', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Conseil et services aux entreprises/Communication');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Communication') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Communication', 'Communication et relations publiques', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Conseil et services aux entreprises/Traduction');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Traduction') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Traduction', 'Traduction et interprétation', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Conseil et services aux entreprises/Impression');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Impression') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Impression', 'Impression et reprographie', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Services à la personne/Ménage et nettoyage');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Ménage et nettoyage') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Ménage et nettoyage', 'Nettoyage à domicile ou professionnel', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Services à la personne/Garde d''enfants');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Garde d''enfants') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Garde d''enfants', 'Garde et accompagnement d''enfants', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Services à la personne/Jardinage');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Jardinage') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Jardinage', 'Entretien des espaces verts', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Services à la personne/Blanchisserie et pressing');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Pressing et blanchisserie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Pressing et blanchisserie', 'Lavage, repassage et pressing', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Services à la personne/Gardiennage');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Gardiennage') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Gardiennage', 'Gardiennage et surveillance de proximité', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Communication et médias/Photographie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Photographie') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Photographie', 'Prestations photographiques', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Communication et médias/Vidéo');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Production vidéo') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Production vidéo', 'Captation et production vidéo', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Communication et médias/Design');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Design') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Design', 'Création graphique et design', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Communication et médias/Publicité');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Publicité') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Publicité', 'Prestations publicitaires', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Événementiel/Mariage');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Prestations mariage') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Prestations mariage', 'Organisation et prestations pour mariage', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Événementiel/Décoration');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Décoration événementielle') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Décoration événementielle', 'Décoration de cérémonies et événements', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Événementiel/Animation');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Animation événementielle') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Animation événementielle', 'Animation, DJ et divertissement', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Événementiel/Photo et vidéo');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Photo et vidéo événementielle') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Photo et vidéo événementielle', 'Couverture photo et vidéo', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Événementiel/Location de matériel');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Location matériel événementiel') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Location matériel événementiel', 'Location de matériel événementiel', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Artisanat et réparation/Couture');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Couture') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Couture', 'Confection et retouche', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Artisanat et réparation/Artisanat');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Artisanat') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Artisanat', 'Produits artisanaux', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Artisanat et réparation/Menuiserie');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Menuiserie artisanale') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Menuiserie artisanale', 'Fabrication et réparation de mobilier', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Artisanat et réparation/Soudure');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Soudure') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Soudure', 'Travaux de soudure', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Artisanat et réparation/Réparation électronique');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Réparation électronique') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Réparation électronique', 'Réparation de matériels électroniques', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Culture loisirs et sport/Sport');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Activités sportives') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Activités sportives', 'Activités et prestations sportives', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Culture loisirs et sport/Fitness');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Fitness et coaching') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Fitness et coaching', 'Fitness, musculation et coaching', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Culture loisirs et sport/Musique');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Cours de musique') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Cours de musique', 'Cours et prestations musicales', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Culture loisirs et sport/Loisirs');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Activités de loisirs') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Activités de loisirs', 'Activités récréatives et loisirs', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Culture loisirs et sport/Livres et médias');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Livres et médias') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Livres et médias', 'Livres, médias et supports culturels', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Tourisme et voyage/Hébergement');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Hébergement touristique') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Hébergement touristique', 'Hébergement pour voyageurs', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Tourisme et voyage/Excursions');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Excursions') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Excursions', 'Excursions et visites', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Tourisme et voyage/Guides');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Guide touristique') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Guide touristique', 'Guides et accompagnement touristique', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Tourisme et voyage/Transport touristique');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Transport touristique') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Transport touristique', 'Transport pour voyageurs', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Animaux/Alimentation animale');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Alimentation animale') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Alimentation animale', 'Nourriture pour animaux', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Animaux/Soins et vétérinaire');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Soins vétérinaires') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Soins vétérinaires', 'Soins et prestations vétérinaires', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Animaux/Animaux');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Animaux') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Animaux', 'Vente ou mise en relation pour animaux', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Énergie et environnement/Solaire');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Équipements solaires') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Équipements solaires', 'Panneaux, batteries et équipements solaires', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Énergie et environnement/Électricité');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Équipements électriques') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Équipements électriques', 'Équipements électriques et énergie', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Énergie et environnement/Déchets');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Gestion des déchets') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Gestion des déchets', 'Collecte, tri et valorisation des déchets', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Énergie et environnement/Eau');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Services liés à l''eau') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Services liés à l''eau', 'Équipements et services liés à l''eau', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Sécurité et sûreté/Sécurité privée');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Sécurité privée') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Sécurité privée', 'Gardiennage et sécurité privée', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Sécurité et sûreté/Vidéosurveillance');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Vidéosurveillance') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Vidéosurveillance', 'Installation et maintenance de vidéosurveillance', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Sécurité et sûreté/Alarme');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Systèmes d''alarme') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Systèmes d''alarme', 'Alarmes et systèmes de sécurité', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Associations et action sociale/Action sociale');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Action sociale') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Action sociale', 'Actions et services sociaux', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Associations et action sociale/Éducation');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Actions éducatives') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Actions éducatives', 'Programmes éducatifs associatifs', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Associations et action sociale/Santé');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Actions santé') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Actions santé', 'Programmes et actions de santé', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Associations et action sociale/Environnement');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Actions environnementales') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Actions environnementales', 'Protection de l''environnement', TRUE);
    END IF;
    v_cat := nexora_ensure_category_path('Associations et action sociale/Culture');
    IF NOT EXISTS (SELECT 1 FROM type_offre WHERE id_categorie = v_cat AND libelle = 'Actions culturelles') THEN
        INSERT INTO type_offre(id_categorie, libelle, description, actif) VALUES (v_cat, 'Actions culturelles', 'Actions culturelles et artistiques', TRUE);
    END IF;
END $$;

-- Attributs dynamiques par catégorie (listes déroulantes, essentiellement).
DO $$
DECLARE v_cat BIGINT;
BEGIN
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements femme/Robes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Taille', 'mode_taille', 'LISTE', TRUE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements femme/Robes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Couleur', 'mode_couleur', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements femme/Robes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Matière', 'mode_matiere', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements femme/Robes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Style', 'mode_style', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements femme/Robes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Longueur', 'mode_longueur', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements femme/Robes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Occasion', 'mode_occasion', 'MULTI_LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements homme/Costumes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Taille', 'costume_taille', 'LISTE', TRUE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements homme/Costumes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Couleur', 'costume_couleur', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements homme/Costumes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Coupe', 'costume_coupe', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Vêtements homme/Costumes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Matière', 'costume_matiere', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Tissus/Bazin');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Type de bazin', 'bazin_type', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Tissus/Bazin');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Couleur', 'bazin_couleur', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Tissus/Bazin');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Longueur', 'bazin_longueur', 'NOMBRE', FALSE, TRUE, TRUE, 'm', NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Mode et textile/Tissus/Bazin');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Largeur', 'bazin_largeur', 'NOMBRE', FALSE, TRUE, TRUE, 'm', NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Téléphones et tablettes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Marque', 'mobile_marque', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Téléphones et tablettes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Modèle', 'mobile_modele', 'TEXTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Téléphones et tablettes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Stockage', 'mobile_stockage', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Téléphones et tablettes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'RAM', 'mobile_ram', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Téléphones et tablettes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'État', 'mobile_etat', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Téléphones et tablettes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Couleur', 'mobile_couleur', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Téléphones et tablettes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Garantie', 'mobile_garantie', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Téléphones et tablettes');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Double SIM', 'mobile_double_sim', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Informatique/Ordinateurs portables');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Marque', 'pc_marque', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Informatique/Ordinateurs portables');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Processeur', 'pc_processeur', 'TEXTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Informatique/Ordinateurs portables');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'RAM', 'pc_ram', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Informatique/Ordinateurs portables');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Stockage', 'pc_stockage', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Informatique/Ordinateurs portables');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Taille écran', 'pc_ecran', 'NOMBRE', FALSE, TRUE, TRUE, 'pouces', NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Informatique/Ordinateurs portables');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'État', 'pc_etat', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Informatique/Ordinateurs portables');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Système d''exploitation', 'pc_os', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Commerce/Alimentation/Riz et céréales');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Variété', 'cereale_variete', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Commerce/Alimentation/Riz et céréales');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Origine', 'cereale_origine', 'TEXTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Commerce/Alimentation/Riz et céréales');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Poids', 'cereale_poids', 'NOMBRE', TRUE, TRUE, TRUE, 'kg', NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Commerce/Alimentation/Riz et céréales');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Conditionnement', 'cereale_conditionnement', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Commerce/Alimentation/Riz et céréales');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Bio', 'cereale_bio', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures céréalières/Riz');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Variété', 'agri_riz_variete', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures céréalières/Riz');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Zone de production', 'agri_zone_production', 'TEXTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures céréalières/Riz');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Mode de production', 'agri_mode_production', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures céréalières/Riz');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Quantité disponible', 'agri_quantite', 'NOMBRE', FALSE, TRUE, TRUE, 'kg', NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures céréalières/Riz');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Conditionnement', 'agri_conditionnement', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures maraîchères/Oignon');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Variété', 'oignon_variete', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures maraîchères/Oignon');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Calibre', 'oignon_calibre', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures maraîchères/Oignon');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Origine', 'oignon_origine', 'TEXTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Agriculture et élevage/Cultures maraîchères/Oignon');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Quantité disponible', 'oignon_quantite', 'NOMBRE', FALSE, TRUE, TRUE, 'kg', NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Automobile/Vente de véhicules');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Marque', 'vehicule_marque', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Automobile/Vente de véhicules');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Modèle', 'vehicule_modele', 'TEXTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Automobile/Vente de véhicules');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Année', 'vehicule_annee', 'NOMBRE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Automobile/Vente de véhicules');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Kilométrage', 'vehicule_kilometrage', 'NOMBRE', FALSE, TRUE, TRUE, 'km', NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Automobile/Vente de véhicules');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Carburant', 'vehicule_carburant', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Automobile/Vente de véhicules');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Transmission', 'vehicule_transmission', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Automobile/Vente de véhicules');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Boîte', 'vehicule_boite', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Automobile/Vente de véhicules');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Couleur', 'vehicule_couleur', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Automobile/Vente de véhicules');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'État', 'vehicule_etat', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Automobile/Vente de véhicules');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Première main', 'vehicule_premiere_main', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Services numériques/Développement mobile');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Plateforme', 'dev_mobile_plateforme', 'MULTI_LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Services numériques/Développement mobile');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Technologies', 'dev_mobile_technologies', 'MULTI_LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Services numériques/Développement mobile');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Délai estimé', 'dev_mobile_delai', 'NOMBRE', FALSE, TRUE, TRUE, 'jours', NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Services numériques/Développement mobile');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Intervention à distance', 'dev_mobile_distance', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Informatique/Services numériques/Développement mobile');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Maintenance incluse', 'dev_mobile_maintenance', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Bâtiment et construction/Plomberie');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Type d''intervention', 'plomberie_intervention', 'MULTI_LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Bâtiment et construction/Plomberie');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Intervention à domicile', 'plomberie_domicile', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Bâtiment et construction/Plomberie');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Urgence', 'plomberie_urgence', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Bâtiment et construction/Plomberie');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Zone d''intervention', 'plomberie_zone', 'TEXTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Services à la personne/Ménage et nettoyage');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Type de prestation', 'menage_prestation', 'MULTI_LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Services à la personne/Ménage et nettoyage');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Intervention à domicile', 'menage_domicile', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Services à la personne/Ménage et nettoyage');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Fréquence', 'menage_frequence', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Services à la personne/Ménage et nettoyage');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Zone d''intervention', 'menage_zone', 'TEXTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Restauration/Cuisine sénégalaise/Plats');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Type de plat', 'plat_type', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Restauration/Cuisine sénégalaise/Plats');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Portion', 'plat_portion', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Restauration/Cuisine sénégalaise/Plats');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Épicé', 'plat_epice', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Restauration/Cuisine sénégalaise/Plats');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'À emporter', 'plat_emporter', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Restauration/Cuisine sénégalaise/Plats');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Livraison', 'plat_livraison', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Restauration/Cuisine sénégalaise/Plats');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Halal', 'plat_halal', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Hôtellerie/Hébergement/Chambres');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Type de chambre', 'hotel_chambre_type', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Hôtellerie/Hébergement/Chambres');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Capacité', 'hotel_capacite', 'NOMBRE', FALSE, TRUE, TRUE, 'personnes', NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Hôtellerie/Hébergement/Chambres');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Nombre de lits', 'hotel_lits', 'NOMBRE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Hôtellerie/Hébergement/Chambres');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Climatisation', 'hotel_climatisation', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Hôtellerie/Hébergement/Chambres');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Wi-Fi', 'hotel_wifi', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Hôtellerie/Hébergement/Chambres');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Petit déjeuner inclus', 'hotel_petit_dejeuner', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Hôtellerie/Hébergement/Chambres');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Vue', 'hotel_vue', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Santé/Médecine générale');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Sexe du patient', 'sante_sexe_patient', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Santé/Médecine générale');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Téléconsultation', 'sante_teleconsultation', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Santé/Médecine générale');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Rendez-vous', 'sante_rdv', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Santé/Médecine générale');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Assurance acceptée', 'sante_assurance', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Santé/Médecine générale');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Urgence', 'sante_urgence', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Santé/Pharmacie/Médicaments');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Forme', 'medicament_forme', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Santé/Pharmacie/Médicaments');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Dosage', 'medicament_dosage', 'TEXTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Santé/Pharmacie/Médicaments');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Ordonnance requise', 'medicament_ordonnance', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Santé/Pharmacie/Médicaments');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Générique', 'medicament_generique', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Immobilier/Location/Appartements');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Surface', 'immo_surface', 'NOMBRE', FALSE, TRUE, TRUE, 'm²', NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Immobilier/Location/Appartements');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Nombre de pièces', 'immo_pieces', 'NOMBRE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Immobilier/Location/Appartements');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Chambres', 'immo_chambres', 'NOMBRE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Immobilier/Location/Appartements');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Salles de bain', 'immo_sdb', 'NOMBRE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Immobilier/Location/Appartements');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Meublé', 'immo_meuble', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Immobilier/Location/Appartements');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Climatisation', 'immo_climatisation', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Immobilier/Location/Appartements');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Parking', 'immo_parking', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Immobilier/Location/Appartements');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Ascenseur', 'immo_ascenseur', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Immobilier/Location/Appartements');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Sécurité', 'immo_securite', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Immobilier/Location/Appartements');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Durée minimale', 'immo_duree_min', 'NOMBRE', FALSE, TRUE, TRUE, 'mois', NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Éducation/Formation professionnelle');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Niveau', 'formation_niveau', 'LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Éducation/Formation professionnelle');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Durée', 'formation_duree', 'NOMBRE', FALSE, TRUE, TRUE, 'mois', NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Éducation/Formation professionnelle');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Modalité', 'formation_modalite', 'MULTI_LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Éducation/Formation professionnelle');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Certification', 'formation_certification', 'BOOLEAN', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Éducation/Formation professionnelle');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Langue', 'formation_langue', 'MULTI_LISTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
    v_cat := nexora_ensure_category_path('Éducation/Formation professionnelle');
    INSERT INTO attribut(id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, unite, aide, actif) VALUES (v_cat, 'Diplôme délivré', 'formation_diplome', 'TEXTE', FALSE, TRUE, TRUE, NULL, NULL, TRUE) ON CONFLICT (code) DO NOTHING;
END $$;

-- Valeurs prédéfinies des attributs de type LISTE/MULTI_LISTE.
INSERT INTO valeur_attribut_possible (id_attribut, valeur, ordre_affichage, actif)
SELECT a.id_attribut, v.valeur, v.ordre, TRUE FROM attribut a JOIN (VALUES
    ('mode_couleur', 'Noir', 0),
    ('mode_couleur', 'Blanc', 1),
    ('mode_couleur', 'Rouge', 2),
    ('mode_couleur', 'Bleu', 3),
    ('mode_couleur', 'Vert', 4),
    ('mode_couleur', 'Jaune', 5),
    ('mode_couleur', 'Rose', 6),
    ('mode_couleur', 'Marron', 7),
    ('mode_couleur', 'Beige', 8),
    ('mode_couleur', 'Doré', 9),
    ('mode_couleur', 'Argenté', 10),
    ('mode_couleur', 'Multicolore', 11),
    ('mode_taille', 'XS', 12),
    ('mode_taille', 'S', 13),
    ('mode_taille', 'M', 14),
    ('mode_taille', 'L', 15),
    ('mode_taille', 'XL', 16),
    ('mode_taille', 'XXL', 17),
    ('mode_taille', 'Sur mesure', 18),
    ('mode_matiere', 'Coton', 19),
    ('mode_matiere', 'Bazin', 20),
    ('mode_matiere', 'Wax', 21),
    ('mode_matiere', 'Dentelle', 22),
    ('mode_matiere', 'Soie', 23),
    ('mode_matiere', 'Lin', 24),
    ('mode_matiere', 'Polyester', 25),
    ('mode_style', 'Classique', 26),
    ('mode_style', 'Moderne', 27),
    ('mode_style', 'Traditionnel', 28),
    ('mode_style', 'Casual', 29),
    ('mode_style', 'Luxe', 30),
    ('mode_style', 'Streetwear', 31),
    ('mode_longueur', 'Court', 32),
    ('mode_longueur', 'Mi-long', 33),
    ('mode_longueur', 'Long', 34),
    ('mode_occasion', 'Quotidien', 35),
    ('mode_occasion', 'Bureau', 36),
    ('mode_occasion', 'Mariage', 37),
    ('mode_occasion', 'Tabaski', 38),
    ('mode_occasion', 'Korité', 39),
    ('mode_occasion', 'Soirée', 40),
    ('mode_occasion', 'Cérémonie', 41),
    ('mobile_stockage', '32 Go', 42),
    ('mobile_stockage', '64 Go', 43),
    ('mobile_stockage', '128 Go', 44),
    ('mobile_stockage', '256 Go', 45),
    ('mobile_stockage', '512 Go', 46),
    ('mobile_stockage', '1 To', 47),
    ('mobile_ram', '2 Go', 48),
    ('mobile_ram', '4 Go', 49),
    ('mobile_ram', '6 Go', 50),
    ('mobile_ram', '8 Go', 51),
    ('mobile_ram', '12 Go', 52),
    ('mobile_ram', '16 Go', 53),
    ('mobile_etat', 'Neuf', 54),
    ('mobile_etat', 'Comme neuf', 55),
    ('mobile_etat', 'Très bon état', 56),
    ('mobile_etat', 'Bon état', 57),
    ('mobile_etat', 'À réparer', 58),
    ('pc_ram', '4 Go', 59),
    ('pc_ram', '8 Go', 60),
    ('pc_ram', '16 Go', 61),
    ('pc_ram', '32 Go', 62),
    ('pc_ram', '64 Go', 63),
    ('pc_stockage', '128 Go SSD', 64),
    ('pc_stockage', '256 Go SSD', 65),
    ('pc_stockage', '512 Go SSD', 66),
    ('pc_stockage', '1 To SSD', 67),
    ('pc_os', 'Windows', 68),
    ('pc_os', 'Linux', 69),
    ('pc_os', 'macOS', 70),
    ('pc_os', 'Sans système', 71),
    ('pc_etat', 'Neuf', 72),
    ('pc_etat', 'Reconditionné', 73),
    ('pc_etat', 'Occasion', 74),
    ('cereale_conditionnement', 'Sac', 75),
    ('cereale_conditionnement', 'Sachet', 76),
    ('cereale_conditionnement', 'Vrac', 77),
    ('cereale_conditionnement', 'Carton', 78),
    ('agri_mode_production', 'Conventionnel', 79),
    ('agri_mode_production', 'Biologique', 80),
    ('agri_mode_production', 'Irrigué', 81),
    ('agri_mode_production', 'Pluvial', 82),
    ('agri_conditionnement', 'Vrac', 83),
    ('agri_conditionnement', 'Sac', 84),
    ('agri_conditionnement', 'Carton', 85),
    ('oignon_calibre', 'Petit', 86),
    ('oignon_calibre', 'Moyen', 87),
    ('oignon_calibre', 'Gros', 88),
    ('vehicule_carburant', 'Essence', 89),
    ('vehicule_carburant', 'Diesel', 90),
    ('vehicule_carburant', 'Hybride', 91),
    ('vehicule_carburant', 'Électrique', 92),
    ('vehicule_transmission', 'Traction', 93),
    ('vehicule_transmission', 'Propulsion', 94),
    ('vehicule_transmission', '4x4', 95),
    ('vehicule_boite', 'Manuelle', 96),
    ('vehicule_boite', 'Automatique', 97),
    ('vehicule_boite', 'Automatisée', 98),
    ('vehicule_etat', 'Neuf', 99),
    ('vehicule_etat', 'Occasion', 100),
    ('vehicule_etat', 'Importé', 101),
    ('plat_portion', 'Individuelle', 102),
    ('plat_portion', 'Duo', 103),
    ('plat_portion', 'Familiale', 104),
    ('plat_type', 'Plat principal', 105),
    ('plat_type', 'Entrée', 106),
    ('plat_type', 'Dessert', 107),
    ('plat_type', 'Petit déjeuner', 108),
    ('hotel_chambre_type', 'Standard', 109),
    ('hotel_chambre_type', 'Supérieure', 110),
    ('hotel_chambre_type', 'Deluxe', 111),
    ('hotel_chambre_type', 'Suite', 112),
    ('hotel_vue', 'Ville', 113),
    ('hotel_vue', 'Mer', 114),
    ('hotel_vue', 'Piscine', 115),
    ('hotel_vue', 'Jardin', 116),
    ('sante_sexe_patient', 'Tous', 117),
    ('sante_sexe_patient', 'Femme', 118),
    ('sante_sexe_patient', 'Homme', 119),
    ('medicament_forme', 'Comprimé', 120),
    ('medicament_forme', 'Gélule', 121),
    ('medicament_forme', 'Sirop', 122),
    ('medicament_forme', 'Crème', 123),
    ('medicament_forme', 'Pommade', 124),
    ('medicament_forme', 'Injection', 125),
    ('medicament_forme', 'Solution', 126),
    ('formation_niveau', 'Débutant', 127),
    ('formation_niveau', 'Intermédiaire', 128),
    ('formation_niveau', 'Avancé', 129),
    ('formation_niveau', 'Professionnel', 130),
    ('formation_modalite', 'Présentiel', 131),
    ('formation_modalite', 'En ligne', 132),
    ('formation_modalite', 'Hybride', 133),
    ('formation_langue', 'Français', 134),
    ('formation_langue', 'Wolof', 135),
    ('formation_langue', 'Anglais', 136)
) AS v(code, valeur, ordre) ON v.code = a.code
WHERE NOT EXISTS (SELECT 1 FROM valeur_attribut_possible p WHERE p.id_attribut = a.id_attribut AND p.valeur = v.valeur);

-- Tags transversaux (indépendants de la catégorie, appliqués aux offres).
INSERT INTO tag(nom, couleur, icone, description, actif) VALUES
    ('Nouveau', '#0f766e', 'sparkles', 'Offre récemment publiée', TRUE),
    ('Promotion', '#dc2626', 'badge-percent', 'Offre en promotion', TRUE),
    ('Meilleure vente', '#d97706', 'trending-up', 'Offre très demandée', TRUE),
    ('Populaire', '#7c3aed', 'flame', 'Offre populaire', TRUE),
    ('Disponible', '#16a34a', 'check-circle', 'Offre actuellement disponible', TRUE),
    ('Rupture imminente', '#ea580c', 'alert-triangle', 'Stock ou disponibilité limité', TRUE),
    ('Livraison', '#2563eb', 'truck', 'Livraison disponible', TRUE),
    ('Retrait sur place', '#0f766e', 'store', 'Retrait possible sur place', TRUE),
    ('Sur rendez-vous', '#8b5cf6', 'calendar', 'Prestation sur rendez-vous', TRUE),
    ('Intervention à domicile', '#0891b2', 'house', 'Intervention possible au domicile', TRUE),
    ('À distance', '#2563eb', 'globe', 'Prestation réalisable à distance', TRUE),
    ('Urgence', '#dc2626', 'siren', 'Intervention urgente disponible', TRUE),
    ('Sur mesure', '#b45309', 'ruler', 'Produit ou prestation personnalisable', TRUE),
    ('Personnalisable', '#b45309', 'palette', 'Personnalisation possible', TRUE),
    ('Artisanal', '#92400e', 'hammer', 'Produit fabriqué artisanalement', TRUE),
    ('Local', '#15803d', 'map-pin', 'Produit ou service local', TRUE),
    ('Made in Sénégal', '#166534', 'flag', 'Produit fabriqué au Sénégal', TRUE),
    ('Importé', '#475569', 'plane', 'Produit importé', TRUE),
    ('Vérifié', '#0f766e', 'badge-check', 'Information vérifiée par Nexora', TRUE),
    ('Certifié', '#d4a72c', 'award', 'Professionnel ou offre certifiée', TRUE),
    ('Professionnel', '#123c35', 'briefcase-business', 'Offre professionnelle', TRUE),
    ('Premium', '#d4a72c', 'crown', 'Offre positionnée premium', TRUE),
    ('Économique', '#16a34a', 'wallet', 'Offre à prix accessible', TRUE),
    ('Négociable', '#7c3aed', 'handshake', 'Prix négociable', TRUE),
    ('Sans rendez-vous', '#16a34a', 'clock', 'Accessible sans rendez-vous', TRUE),
    ('24h/24', '#dc2626', 'clock-3', 'Disponible 24 heures sur 24', TRUE),
    ('Week-end', '#2563eb', 'calendar-days', 'Disponible le week-end', TRUE),
    ('Livraison Dakar', '#2563eb', 'map', 'Livraison dans Dakar', TRUE),
    ('Livraison nationale', '#2563eb', 'map', 'Livraison partout au Sénégal', TRUE),
    ('Paiement mobile', '#f59e0b', 'smartphone', 'Paiement mobile disponible', TRUE),
    ('Orange Money', '#f97316', 'wallet', 'Orange Money accepté', TRUE),
    ('Wave', '#0ea5e9', 'waves', 'Wave accepté', TRUE),
    ('Carte bancaire', '#475569', 'credit-card', 'Carte bancaire acceptée', TRUE),
    ('Réservation en ligne', '#8b5cf6', 'calendar-check', 'Réservation possible en ligne', TRUE),
    ('Commande en ligne', '#0f766e', 'shopping-cart', 'Commande possible en ligne', TRUE),
    ('Disponible immédiatement', '#16a34a', 'zap', 'Disponibilité immédiate', TRUE),
    ('Neuf', '#16a34a', 'package-check', 'Produit neuf', TRUE),
    ('Occasion', '#64748b', 'refresh-cw', 'Produit d''occasion', TRUE),
    ('Reconditionné', '#2563eb', 'rotate-ccw', 'Produit reconditionné', TRUE),
    ('Garantie', '#0f766e', 'shield-check', 'Garantie disponible', TRUE),
    ('Halal', '#15803d', 'badge-check', 'Produit ou restauration halal', TRUE),
    ('Bio', '#16a34a', 'leaf', 'Produit issu d''une démarche biologique', TRUE),
    ('Sans sucre', '#16a34a', 'circle-off', 'Produit sans sucre ajouté', TRUE),
    ('Sans gluten', '#16a34a', 'wheat-off', 'Produit sans gluten', TRUE),
    ('Végétarien', '#16a34a', 'salad', 'Option végétarienne', TRUE),
    ('Vegan', '#16a34a', 'sprout', 'Option vegan', TRUE),
    ('Famille', '#2563eb', 'users', 'Adapté aux familles', TRUE),
    ('Enfants', '#0891b2', 'baby', 'Adapté aux enfants', TRUE),
    ('Étudiants', '#2563eb', 'graduation-cap', 'Adapté aux étudiants', TRUE),
    ('Entreprises', '#475569', 'building-2', 'Destiné aux entreprises', TRUE),
    ('Femmes', '#db2777', 'venus', 'Destiné principalement aux femmes', TRUE),
    ('Hommes', '#2563eb', 'mars', 'Destiné principalement aux hommes', TRUE),
    ('Bébé', '#ec4899', 'baby', 'Destiné aux bébés', TRUE),
    ('Cérémonie', '#d4a72c', 'gem', 'Adapté aux cérémonies', TRUE),
    ('Mariage', '#d4a72c', 'heart', 'Adapté aux mariages', TRUE),
    ('Tabaski', '#15803d', 'calendar-heart', 'Offre pertinente pour la Tabaski', TRUE),
    ('Korité', '#15803d', 'moon-star', 'Offre pertinente pour la Korité', TRUE),
    ('Ramadan', '#15803d', 'moon', 'Offre pertinente pour le Ramadan', TRUE)
ON CONFLICT (nom) DO NOTHING;

DROP FUNCTION IF EXISTS nexora_ensure_category_path(TEXT);
