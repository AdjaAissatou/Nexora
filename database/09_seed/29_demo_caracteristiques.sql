-- ============================================================================
-- Démonstration : offres rangées dans la bonne catégorie et caractéristiques renseignées
-- (tailles, couleurs, matière, pointures, stockage…) pour que la fiche article montre ce
-- qu'un client veut savoir avant de se déplacer.
--
-- Fonctionne sur les offres de démonstration du dépôt comme sur celles des packs locaux :
-- les offres sont reconnues par leur titre. Rejouable : une caractéristique déjà
-- renseignée pour une offre n'est jamais modifiée.
-- ============================================================================

SET client_encoding = 'UTF8';

-- Chemin complet d'une catégorie : « Mode et textile > Vêtements homme > Chemises ».
CREATE OR REPLACE FUNCTION pg_temp.chemin(id_ BIGINT) RETURNS TEXT AS $$
    WITH RECURSIVE ch(id, parent, nom, niveau) AS (
        SELECT id_categorie, id_categorie_parent, nom::TEXT, 0 FROM categorie WHERE id_categorie = id_
        UNION ALL
        SELECT c.id_categorie, c.id_categorie_parent, c.nom::TEXT, ch.niveau + 1 FROM categorie c JOIN ch ON c.id_categorie = ch.parent
    )
    SELECT string_agg(nom, ' > ' ORDER BY niveau DESC) FROM ch
$$ LANGUAGE sql;

-- Range les offres dont le titre correspond au motif, si elles sont dans une catégorie trop vague.
CREATE OR REPLACE FUNCTION pg_temp.ranger(motif TEXT, chemin_ TEXT, type_ TEXT) RETURNS VOID AS $$
DECLARE cat BIGINT; typ BIGINT;
BEGIN
    SELECT c.id_categorie INTO cat FROM categorie c WHERE pg_temp.chemin(c.id_categorie) = chemin_;
    SELECT t.id_type_offre INTO typ FROM type_offre t WHERE t.id_categorie = cat AND t.libelle = type_ AND t.actif IS NOT FALSE;
    IF cat IS NULL OR typ IS NULL THEN RETURN; END IF;
    UPDATE offre o SET id_categorie = cat, id_type_offre = typ
    WHERE o.titre ~* motif
      AND pg_temp.chemin(o.id_categorie) IN ('Commerce', 'Services', 'Transport',
                                              'Artisanat et réparation > Réparation > Électroménager');
END $$ LANGUAGE plpgsql;

-- Valeurs proposées (LISTE, MULTI_LISTE) ; « epuises » : celles affichées barrées.
CREATE OR REPLACE FUNCTION pg_temp.mettre(offre_ BIGINT, code_ TEXT, valeurs TEXT[], epuises TEXT[] DEFAULT '{}')
RETURNS VOID AS $$
DECLARE ida BIGINT;
BEGIN
    SELECT id_attribut INTO ida FROM attribut WHERE code = code_;
    IF ida IS NULL OR EXISTS (SELECT 1 FROM offre_attribut WHERE id_offre = offre_ AND id_attribut = ida) THEN RETURN; END IF;
    INSERT INTO offre_attribut (id_offre, id_attribut, id_valeur, epuise)
    SELECT offre_, ida, v.id_valeur, v.valeur = ANY (epuises)
    FROM valeur_attribut_possible v
    WHERE v.id_attribut = ida AND v.valeur = ANY (valeurs || epuises)
    ORDER BY v.ordre_affichage;
END $$ LANGUAGE plpgsql;

-- Valeur libre (TEXTE, BOOLEAN) ou numérique (NOMBRE).
CREATE OR REPLACE FUNCTION pg_temp.ecrire(offre_ BIGINT, code_ TEXT, texte TEXT, nombre NUMERIC DEFAULT NULL)
RETURNS VOID AS $$
DECLARE ida BIGINT;
BEGIN
    SELECT id_attribut INTO ida FROM attribut WHERE code = code_;
    IF ida IS NULL OR (texte IS NULL AND nombre IS NULL)
       OR EXISTS (SELECT 1 FROM offre_attribut WHERE id_offre = offre_ AND id_attribut = ida) THEN RETURN; END IF;
    INSERT INTO offre_attribut (id_offre, id_attribut, valeur_texte, valeur_nombre) VALUES (offre_, ida, texte, nombre);
END $$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------------
-- 1. Offres rangées trop haut (« Commerce », « Services », « Transport ») ou dans la réparation
--    alors qu'elles sont vendues
-- ---------------------------------------------------------------------------
DO $$
BEGIN
    PERFORM pg_temp.ranger('plomberie|sanitaire|canalisation|chauffe-eau', 'Bâtiment et construction > Plomberie', 'Plomberie');
    PERFORM pg_temp.ranger('meuble|mobilier|portes et fenêtres', 'Bâtiment et construction > Menuiserie > Menuiserie bois', 'Menuiserie bois');
    PERFORM pg_temp.ranger('livraison|colis', 'Transport > Livraison > Livraison colis', 'Livraison colis');
    PERFORM pg_temp.ranger('vtc|course', 'Transport > Transport urbain > VTC', 'VTC');
    PERFORM pg_temp.ranger('chauffeur', 'Transport > Location de véhicule', 'Location avec chauffeur');
    PERFORM pg_temp.ranger('chemise', 'Mode et textile > Vêtements homme > Chemises', 'Chemises homme');
    PERFORM pg_temp.ranger('boubou|tunique', 'Mode et textile > Vêtements homme > Boubous', 'Boubous homme');
    PERFORM pg_temp.ranger('pantalon', 'Mode et textile > Vêtements homme > Pantalons', 'Pantalons homme');
    PERFORM pg_temp.ranger('robe', 'Mode et textile > Vêtements femme > Robes', 'Robes');
    PERFORM pg_temp.ranger('chaussure', 'Mode et textile > Chaussures > Homme', 'Chaussures homme');
    PERFORM pg_temp.ranger('ceinture', 'Mode et textile > Accessoires > Ceintures', 'Ceintures');
    PERFORM pg_temp.ranger('cravate', 'Mode et textile > Accessoires > Foulards', 'Foulards');
    PERFORM pg_temp.ranger('montre', 'Mode et textile > Accessoires > Bijoux', 'Bijoux');
    PERFORM pg_temp.ranger('riz', 'Commerce > Alimentation > Riz et céréales', 'Riz et céréales');
    PERFORM pg_temp.ranger('sucre|lait|eau min|huile', 'Commerce > Alimentation > Épicerie', 'Produits d''épicerie');
    PERFORM pg_temp.ranger('réfrigérateur', 'Électronique et électroménager > Électroménager > Réfrigérateurs', 'Réfrigérateurs');
    PERFORM pg_temp.ranger('machine à laver', 'Électronique et électroménager > Électroménager > Machines à laver', 'Machines à laver');
    PERFORM pg_temp.ranger('blender|bouilloire|fer à repasser|ventilateur|friteuse|four|climatiseur|fontaine',
                          'Commerce > Électroménager', 'Électroménager');
END $$;

-- ---------------------------------------------------------------------------
-- 2. Caractéristiques, déduites du titre
-- ---------------------------------------------------------------------------
DO $$
DECLARE
    o RECORD;
    t TEXT;
    ch TEXT;
    pair BOOLEAN;
    contenance TEXT;
BEGIN
    FOR o IN SELECT id_offre, titre, id_categorie FROM offre ORDER BY id_offre LOOP
        t := lower(o.titre);
        ch := pg_temp.chemin(o.id_categorie);
        pair := o.id_offre % 2 = 0;

        IF ch = 'Mode et textile > Vêtements homme > Chemises' THEN
            PERFORM pg_temp.mettre(o.id_offre, 'mt_couleurs', CASE
                WHEN t ~ 'blanc' THEN ARRAY['Blanc'] WHEN t ~ 'beige' THEN ARRAY['Beige', 'Blanc']
                WHEN t ~ 'bleu' THEN ARRAY['Bleu ciel', 'Bleu'] ELSE ARRAY['Blanc', 'Bleu ciel', 'Noir'] END);
            PERFORM pg_temp.mettre(o.id_offre, 'mt_matiere', ARRAY[CASE WHEN t ~ '\mlin\M' THEN 'Lin' ELSE 'Coton' END]);
            PERFORM pg_temp.mettre(o.id_offre, 'mt_fabrication', ARRAY[CASE WHEN t ~ 'mao' THEN 'Fabriqué au Sénégal' ELSE 'Importé' END]);
            PERFORM pg_temp.mettre(o.id_offre, 'vet_homme_tailles', ARRAY['S', 'M', 'L', 'XL'], CASE WHEN pair THEN ARRAY['XXL'] ELSE ARRAY['S'] END);
            PERFORM pg_temp.mettre(o.id_offre, 'vet_homme_coupe', ARRAY[CASE WHEN t ~ 'cintr' THEN 'Cintrée' ELSE 'Droite (regular)' END]);
            PERFORM pg_temp.mettre(o.id_offre, 'vet_homme_manches', ARRAY['Longues']);
            PERFORM pg_temp.mettre(o.id_offre, 'chemise_col', ARRAY[CASE WHEN t ~ 'mao' THEN 'Mao (officier)' ELSE 'Classique' END]);
            PERFORM pg_temp.ecrire(o.id_offre, 'mt_entretien', CASE WHEN t ~ '\mlin\M'
                THEN 'Lavage à 30 °C, repasser humide' ELSE 'Lavage à 40 °C, repassage fer moyen' END);

        ELSIF ch = 'Mode et textile > Vêtements homme > Pantalons' THEN
            PERFORM pg_temp.mettre(o.id_offre, 'mt_couleurs', CASE
                WHEN t ~ 'jean' THEN ARRAY['Bleu', 'Noir'] WHEN t ~ 'bleu' THEN ARRAY['Bleu marine'] WHEN t ~ 'gris' THEN ARRAY['Gris']
                WHEN t ~ 'marron' THEN ARRAY['Marron', 'Beige'] ELSE ARRAY['Noir', 'Bleu marine'] END);
            PERFORM pg_temp.mettre(o.id_offre, 'mt_matiere', ARRAY[CASE WHEN t ~ 'jean' THEN 'Jean (denim)'
                WHEN t ~ 'costume' THEN 'Laine' ELSE 'Coton' END]);
            PERFORM pg_temp.mettre(o.id_offre, 'vet_homme_tailles', ARRAY['S', 'M', 'L', 'XL', 'XXL'], CASE WHEN pair THEN ARRAY['XS'] ELSE '{}' END);
            PERFORM pg_temp.mettre(o.id_offre, 'vet_homme_coupe', ARRAY[CASE WHEN t ~ 'slim' THEN 'Ajustée (slim)' ELSE 'Droite (regular)' END]);
            PERFORM pg_temp.mettre(o.id_offre, 'mt_fabrication', ARRAY['Importé']);

        ELSIF ch = 'Mode et textile > Vêtements homme > Boubous' THEN
            PERFORM pg_temp.mettre(o.id_offre, 'mt_couleurs', ARRAY['Blanc', 'Bleu', 'Bordeaux']);
            PERFORM pg_temp.mettre(o.id_offre, 'mt_matiere', ARRAY['Bazin']);
            PERFORM pg_temp.mettre(o.id_offre, 'mt_fabrication', ARRAY['Fabriqué au Sénégal', 'Sur mesure']);
            PERFORM pg_temp.mettre(o.id_offre, 'vet_homme_tailles', ARRAY['M', 'L', 'XL', 'XXL', '3XL']);
            PERFORM pg_temp.mettre(o.id_offre, 'vet_homme_manches', ARRAY['Longues']);
            PERFORM pg_temp.ecrire(o.id_offre, 'mt_entretien', 'Lavage à la main, pas de sèche-linge');

        ELSIF ch = 'Mode et textile > Vêtements femme > Robes' THEN
            PERFORM pg_temp.mettre(o.id_offre, 'mt_couleurs', ARRAY['Multicolore']);
            PERFORM pg_temp.mettre(o.id_offre, 'mt_matiere', ARRAY[CASE WHEN t ~ 'wax' THEN 'Wax' ELSE 'Coton' END]);
            PERFORM pg_temp.mettre(o.id_offre, 'mt_fabrication', ARRAY['Fabriqué au Sénégal']);
            PERFORM pg_temp.mettre(o.id_offre, 'vet_femme_tailles', ARRAY['S', 'M', 'XL'], ARRAY['L']);
            PERFORM pg_temp.mettre(o.id_offre, 'vet_femme_coupe', ARRAY['Évasée']);
            PERFORM pg_temp.mettre(o.id_offre, 'vet_femme_manches', ARRAY['Courtes']);

        ELSIF ch = 'Mode et textile > Chaussures > Homme' THEN
            PERFORM pg_temp.mettre(o.id_offre, 'mt_couleurs', CASE
                WHEN t ~ 'bordeaux' THEN ARRAY['Bordeaux'] WHEN t ~ 'noir' THEN ARRAY['Noir']
                WHEN t ~ 'chelsea' THEN ARRAY['Marron', 'Noir'] ELSE ARRAY['Noir', 'Marron'] END);
            PERFORM pg_temp.mettre(o.id_offre, 'mt_matiere', ARRAY[CASE WHEN t ~ 'daim' THEN 'Daim' ELSE 'Cuir' END]);
            PERFORM pg_temp.mettre(o.id_offre, 'chau_homme_pointures', ARRAY['40', '41', '42', '43', '44'],
                CASE WHEN pair THEN ARRAY['45'] ELSE ARRAY['42'] END);
            PERFORM pg_temp.mettre(o.id_offre, 'chau_semelle', ARRAY[CASE WHEN t ~ 'chelsea|daim' THEN 'Gomme' ELSE 'Cuir' END]);
            PERFORM pg_temp.mettre(o.id_offre, 'chau_fermeture', ARRAY[CASE WHEN t ~ 'chelsea' THEN 'Élastiques' ELSE 'Lacets' END]);
            PERFORM pg_temp.mettre(o.id_offre, 'mt_fabrication', ARRAY['Importé']);

        ELSIF ch LIKE 'Mode et textile > Accessoires%' THEN
            PERFORM pg_temp.mettre(o.id_offre, 'mt_couleurs', CASE
                WHEN t ~ 'noire et or' THEN ARRAY['Noir', 'Doré'] WHEN t ~ 'doré' THEN ARRAY['Doré']
                WHEN t ~ 'bleu' THEN ARRAY['Bleu', 'Argenté'] WHEN t ~ 'motif' THEN ARRAY['Multicolore']
                WHEN t ~ 'cravate' THEN ARRAY['Noir', 'Bleu marine', 'Bordeaux', 'Gris']
                ELSE ARRAY['Noir', 'Marron'] END);
            IF t ~ 'ceinture' THEN PERFORM pg_temp.mettre(o.id_offre, 'mt_matiere', ARRAY['Cuir']);
            ELSIF t ~ 'cravate' THEN PERFORM pg_temp.mettre(o.id_offre, 'mt_matiere', ARRAY['Soie']);
            END IF;
            PERFORM pg_temp.mettre(o.id_offre, 'mt_fabrication', ARRAY['Importé']);

        ELSIF ch = 'Électronique et électroménager > Téléphonie > Smartphones' THEN
            PERFORM pg_temp.mettre(o.id_offre, 'elec_couleurs', CASE
                WHEN t ~ 'orange' THEN ARRAY['Orange', 'Argenté'] WHEN t ~ 'coloris' THEN ARRAY['Noir', 'Blanc', 'Bleu', 'Rose']
                ELSE ARRAY['Noir', 'Bleu'] END, CASE WHEN t ~ 'coloris' AND pair THEN ARRAY['Rose'] ELSE '{}' END);
            PERFORM pg_temp.mettre(o.id_offre, 'tel_stockage', CASE WHEN t ~ 'iphone'
                THEN ARRAY['128 Go', '256 Go'] ELSE ARRAY['128 Go', '256 Go'] END, CASE WHEN t ~ 'iphone' THEN ARRAY['512 Go'] ELSE '{}' END);
            PERFORM pg_temp.mettre(o.id_offre, 'tel_ram', ARRAY[CASE WHEN t ~ 'iphone' THEN '8 Go' ELSE '12 Go' END]);
            PERFORM pg_temp.ecrire(o.id_offre, 'tel_ecran', NULL, CASE WHEN t ~ 'note|grand' THEN 6.8 ELSE 6.3 END);
            PERFORM pg_temp.ecrire(o.id_offre, 'tel_double_sim', 'true');
            PERFORM pg_temp.mettre(o.id_offre, 'elec_etat', ARRAY['Neuf']);
            PERFORM pg_temp.ecrire(o.id_offre, 'elec_garantie', NULL, 12);

        ELSIF ch LIKE 'Électronique et électroménager > Électroménager%' THEN
            PERFORM pg_temp.mettre(o.id_offre, 'elec_couleurs', ARRAY[CASE WHEN t ~ 'inox' THEN 'Argenté' ELSE 'Blanc' END]);
            PERFORM pg_temp.mettre(o.id_offre, 'electromenager_energie', ARRAY['A+']);
            IF t ~ 'réfrigérateur' THEN PERFORM pg_temp.ecrire(o.id_offre, 'electromenager_capacite', NULL, 300); END IF;
            PERFORM pg_temp.mettre(o.id_offre, 'elec_etat', ARRAY['Neuf']);
            PERFORM pg_temp.ecrire(o.id_offre, 'elec_garantie', NULL, 24);

        ELSIF ch = 'Commerce > Électroménager' THEN
            PERFORM pg_temp.ecrire(o.id_offre, 'commerce_electromenager_puissance', NULL, CASE
                WHEN t ~ 'blender' THEN 600 WHEN t ~ 'bouilloire' THEN 2200 WHEN t ~ 'fer à repasser' THEN 2400
                WHEN t ~ 'ventilateur' THEN 60 WHEN t ~ 'friteuse' THEN 1500 WHEN t ~ 'four' THEN 1600
                WHEN t ~ 'climatiseur' THEN 1200 WHEN t ~ 'fontaine' THEN 500 END);
            PERFORM pg_temp.ecrire(o.id_offre, 'commerce_electromenager_capacite', NULL,
                CAST(substring(replace(t, ',', '.') FROM '([0-9]+(?:\.[0-9]+)?) ?l\M') AS NUMERIC));
            PERFORM pg_temp.mettre(o.id_offre, 'commerce_electromenager_couleurs', CASE
                WHEN t ~ 'inox' THEN ARRAY['Argenté'] ELSE ARRAY['Blanc', 'Noir'] END);
            PERFORM pg_temp.ecrire(o.id_offre, 'commerce_electromenager_garantie', NULL, 12);

        ELSIF ch LIKE 'Informatique%' THEN
            IF ch LIKE 'Informatique > Téléphones et tablettes%' THEN
                PERFORM pg_temp.mettre(o.id_offre, 'info_tel_compatibilite',
                    ARRAY[CASE WHEN t ~ 'iphone' THEN 'iPhone' ELSE 'Tous téléphones' END]);
            END IF;
            PERFORM pg_temp.mettre(o.id_offre, 'info_etat', ARRAY['Neuf']);
            PERFORM pg_temp.ecrire(o.id_offre, 'info_garantie', NULL, CASE WHEN ch LIKE '%Accessoires' THEN 6 ELSE 12 END);

        ELSIF ch LIKE 'Commerce > Alimentation%' OR ch LIKE 'Agroalimentaire%' THEN
            contenance := substring(o.titre FROM '([0-9]+(?:[,.][0-9]+)? ?(?:kg|g|L|ml|cl)\M(?: ?x ?[0-9]+)?)');
            IF contenance IS NULL AND t ~ 'le kg' THEN contenance := 'Au kilo'; END IF;
            IF contenance IS NULL AND t ~ 'pièce' THEN contenance := 'À la pièce'; END IF;
            PERFORM pg_temp.ecrire(o.id_offre, CASE WHEN ch LIKE 'Agroalimentaire%' THEN 'alim_contenance' ELSE 'commerce_alim_contenance' END, contenance);
            PERFORM pg_temp.mettre(o.id_offre, CASE WHEN ch LIKE 'Agroalimentaire%' THEN 'alim_origine' ELSE 'commerce_alim_origine' END,
                ARRAY[CASE WHEN ch ~ 'Fruits et légumes|Viandes|Boucherie|Poisson' THEN 'Sénégal'
                           WHEN t ~ 'riz' THEN 'Asie' ELSE 'Europe' END]);
            PERFORM pg_temp.mettre(o.id_offre, CASE WHEN ch LIKE 'Agroalimentaire%' THEN 'alim_conservation' ELSE 'commerce_alim_conservation' END,
                ARRAY[CASE WHEN ch ~ 'Viandes|Boucherie|Poisson' OR (ch ~ 'laitiers|Lait|Fromage' AND t !~ 'poudre') OR t ~ 'beurre'
                           THEN 'Au frais' ELSE 'Température ambiante' END]);
        END IF;
    END LOOP;
END $$;
