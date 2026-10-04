-- ============================================================================
-- Caractéristiques des articles (docs/architecture-acteurs.md §14) : ce qu'un client veut
-- savoir avant de se déplacer (tailles et couleurs disponibles, matière, pointures,
-- stockage d'un téléphone, contenance d'un produit…).
--
-- Elles sont HÉRITÉES : une caractéristique posée sur « Mode et textile » vaut pour toutes
-- ses sous-catégories ; celles de « Vêtements homme » s'ajoutent pour les chemises,
-- pantalons… Le formulaire de création d'offre les propose donc toutes.
--
-- Rejouable : une caractéristique est retrouvée par son code, une valeur par son libellé.
-- ============================================================================

SET client_encoding = 'UTF8';

-- Catégorie désignée par son chemin depuis la racine ; NULL si elle n'existe pas.
CREATE OR REPLACE FUNCTION pg_temp.cat(VARIADIC chemin TEXT[]) RETURNS BIGINT AS $$
DECLARE id BIGINT; n TEXT; premier BOOLEAN := TRUE;
BEGIN
    FOREACH n IN ARRAY chemin LOOP
        IF premier THEN
            SELECT c.id_categorie INTO id FROM categorie c WHERE c.id_categorie_parent IS NULL AND c.nom = n;
            premier := FALSE;
        ELSE
            SELECT c.id_categorie INTO id FROM categorie c WHERE c.id_categorie_parent = id AND c.nom = n;
        END IF;
        IF id IS NULL THEN RETURN NULL; END IF;
    END LOOP;
    RETURN id;
END $$ LANGUAGE plpgsql;

-- Ajoute (ou complète) une caractéristique et ses valeurs proposées.
CREATE OR REPLACE FUNCTION pg_temp.carac(categorie BIGINT, code_ TEXT, nom_ TEXT, type_ TEXT, ordre INT,
                                         valeurs TEXT[] DEFAULT NULL, unite_ TEXT DEFAULT NULL, aide_ TEXT DEFAULT NULL)
RETURNS VOID AS $$
DECLARE ida BIGINT; v TEXT; i INT := 0;
BEGIN
    IF categorie IS NULL THEN RETURN; END IF;
    INSERT INTO attribut (id_categorie, nom, code, type_champ, obligatoire, filtrable, affichable, ordre_affichage, unite, aide, actif)
    VALUES (categorie, nom_, code_, CAST(type_ AS type_champ), FALSE, TRUE, TRUE, ordre, unite_, aide_, TRUE)
    ON CONFLICT (code) DO NOTHING;
    SELECT id_attribut INTO ida FROM attribut WHERE code = code_;
    IF valeurs IS NULL THEN RETURN; END IF;
    FOREACH v IN ARRAY valeurs LOOP
        i := i + 1;
        INSERT INTO valeur_attribut_possible (id_attribut, valeur, ordre_affichage, actif)
        SELECT ida, v, i, TRUE WHERE NOT EXISTS (SELECT 1 FROM valeur_attribut_possible WHERE id_attribut = ida AND valeur = v);
    END LOOP;
END $$ LANGUAGE plpgsql;

DO $$
DECLARE
    couleurs TEXT[] := ARRAY['Noir', 'Blanc', 'Gris', 'Beige', 'Marron', 'Bleu', 'Bleu marine', 'Bleu ciel', 'Rouge',
                             'Bordeaux', 'Rose', 'Orange', 'Jaune', 'Vert', 'Kaki', 'Violet', 'Doré', 'Argenté', 'Multicolore'];
    mode BIGINT := pg_temp.cat('Mode et textile');
BEGIN
    -- Mode et textile : pour tous les vêtements, chaussures, accessoires et tissus
    PERFORM pg_temp.carac(mode, 'mt_couleurs', 'Couleurs disponibles', 'MULTI_LISTE', 10, couleurs);
    PERFORM pg_temp.carac(mode, 'mt_matiere', 'Matière', 'LISTE', 20,
        ARRAY['Coton', 'Lin', 'Wax', 'Bazin', 'Soie', 'Laine', 'Polyester', 'Jean (denim)', 'Velours', 'Cuir', 'Daim',
              'Toile', 'Synthétique', 'Mélange']);
    PERFORM pg_temp.carac(mode, 'mt_fabrication', 'Fabrication', 'LISTE', 30,
        ARRAY['Fabriqué au Sénégal', 'Importé', 'Sur mesure']);
    PERFORM pg_temp.carac(mode, 'mt_entretien', 'Entretien', 'TEXTE', 90, NULL, NULL, 'Exemple : lavage à 30 °C, repassage doux');

    -- Vêtements adultes
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Vêtements homme'), 'vet_homme_tailles', 'Tailles disponibles', 'MULTI_LISTE', 1,
        ARRAY['XS', 'S', 'M', 'L', 'XL', 'XXL', '3XL']);
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Vêtements femme'), 'vet_femme_tailles', 'Tailles disponibles', 'MULTI_LISTE', 1,
        ARRAY['XS', 'S', 'M', 'L', 'XL', 'XXL', '3XL']);
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Vêtements homme'), 'vet_homme_coupe', 'Coupe', 'LISTE', 40,
        ARRAY['Ajustée (slim)', 'Cintrée', 'Droite (regular)', 'Ample']);
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Vêtements femme'), 'vet_femme_coupe', 'Coupe', 'LISTE', 40,
        ARRAY['Ajustée', 'Cintrée', 'Droite', 'Évasée', 'Ample']);
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Vêtements homme'), 'vet_homme_manches', 'Manches', 'LISTE', 45,
        ARRAY['Courtes', 'Longues', 'Trois-quarts', 'Sans manches']);
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Vêtements femme'), 'vet_femme_manches', 'Manches', 'LISTE', 45,
        ARRAY['Courtes', 'Longues', 'Trois-quarts', 'Sans manches']);
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Vêtements homme', 'Chemises'), 'chemise_col', 'Col', 'LISTE', 50,
        ARRAY['Classique', 'Mao (officier)', 'Italien', 'Boutonné']);
    -- Vêtements enfant (deux branches existent dans la taxonomie)
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Vêtements enfant'), 'vet_enfant_tailles', 'Tailles disponibles', 'MULTI_LISTE', 1,
        ARRAY['0-3 mois', '3-6 mois', '6-12 mois', '2 ans', '4 ans', '6 ans', '8 ans', '10 ans', '12 ans', '14 ans']);
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Enfant'), 'enfant_tailles', 'Tailles disponibles', 'MULTI_LISTE', 1,
        ARRAY['0-3 mois', '3-6 mois', '6-12 mois', '2 ans', '4 ans', '6 ans', '8 ans', '10 ans', '12 ans', '14 ans']);

    -- Chaussures
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Chaussures', 'Homme'), 'chau_homme_pointures', 'Pointures disponibles', 'MULTI_LISTE', 1,
        ARRAY['38', '39', '40', '41', '42', '43', '44', '45', '46', '47']);
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Chaussures', 'Femme'), 'chau_femme_pointures', 'Pointures disponibles', 'MULTI_LISTE', 1,
        ARRAY['35', '36', '37', '38', '39', '40', '41', '42']);
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Chaussures', 'Enfant'), 'chau_enfant_pointures', 'Pointures disponibles', 'MULTI_LISTE', 1,
        ARRAY['20', '22', '24', '26', '28', '30', '32', '34']);
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Chaussures'), 'chau_semelle', 'Semelle', 'LISTE', 50,
        ARRAY['Cuir', 'Caoutchouc', 'Gomme', 'Synthétique']);
    PERFORM pg_temp.carac(pg_temp.cat('Mode et textile', 'Chaussures'), 'chau_fermeture', 'Fermeture', 'LISTE', 55,
        ARRAY['Lacets', 'Élastiques', 'Scratch', 'Boucle', 'Sans fermeture']);

    -- Téléphones (deux branches : Électronique > Téléphonie et Informatique > Téléphones et tablettes)
    PERFORM pg_temp.carac(pg_temp.cat('Électronique et électroménager', 'Téléphonie'), 'tel_stockage', 'Stockage disponible', 'MULTI_LISTE', 1,
        ARRAY['32 Go', '64 Go', '128 Go', '256 Go', '512 Go', '1 To']);
    PERFORM pg_temp.carac(pg_temp.cat('Électronique et électroménager', 'Téléphonie'), 'tel_ram', 'Mémoire vive', 'LISTE', 5,
        ARRAY['2 Go', '3 Go', '4 Go', '6 Go', '8 Go', '12 Go', '16 Go']);
    PERFORM pg_temp.carac(pg_temp.cat('Électronique et électroménager', 'Téléphonie'), 'tel_ecran', 'Écran', 'NOMBRE', 10, NULL, 'pouces');
    PERFORM pg_temp.carac(pg_temp.cat('Électronique et électroménager', 'Téléphonie'), 'tel_double_sim', 'Double SIM', 'BOOLEAN', 15);
    PERFORM pg_temp.carac(pg_temp.cat('Informatique', 'Téléphones et tablettes'), 'info_tel_compatibilite', 'Compatible avec', 'MULTI_LISTE', 1,
        ARRAY['iPhone', 'Samsung', 'Tecno', 'Infinix', 'Xiaomi', 'Huawei', 'Tous téléphones']);

    -- Électronique, informatique et électroménager : état, garantie, couleurs
    PERFORM pg_temp.carac(pg_temp.cat('Électronique et électroménager'), 'elec_etat', 'État', 'LISTE', 60,
        ARRAY['Neuf', 'Reconditionné', 'Occasion']);
    PERFORM pg_temp.carac(pg_temp.cat('Électronique et électroménager'), 'elec_garantie', 'Garantie', 'NOMBRE', 65, NULL, 'mois');
    PERFORM pg_temp.carac(pg_temp.cat('Électronique et électroménager'), 'elec_couleurs', 'Couleurs disponibles', 'MULTI_LISTE', 20, couleurs);
    PERFORM pg_temp.carac(pg_temp.cat('Informatique'), 'info_etat', 'État', 'LISTE', 60, ARRAY['Neuf', 'Reconditionné', 'Occasion']);
    PERFORM pg_temp.carac(pg_temp.cat('Informatique'), 'info_garantie', 'Garantie', 'NOMBRE', 65, NULL, 'mois');
    PERFORM pg_temp.carac(pg_temp.cat('Électronique et électroménager', 'Électroménager'), 'electromenager_puissance', 'Puissance', 'NOMBRE', 30, NULL, 'W');
    PERFORM pg_temp.carac(pg_temp.cat('Électronique et électroménager', 'Électroménager'), 'electromenager_capacite', 'Capacité', 'NOMBRE', 35, NULL, 'L');
    PERFORM pg_temp.carac(pg_temp.cat('Électronique et électroménager', 'Électroménager'), 'electromenager_energie', 'Classe énergétique', 'LISTE', 40,
        ARRAY['A+++', 'A++', 'A+', 'A', 'B', 'C', 'D']);
    PERFORM pg_temp.carac(pg_temp.cat('Maison et habitat', 'Électroménager'), 'maison_electromenager_puissance', 'Puissance', 'NOMBRE', 30, NULL, 'W');
    PERFORM pg_temp.carac(pg_temp.cat('Maison et habitat', 'Électroménager'), 'maison_electromenager_capacite', 'Capacité', 'NOMBRE', 35, NULL, 'L');
    PERFORM pg_temp.carac(pg_temp.cat('Maison et habitat', 'Électroménager'), 'maison_electromenager_garantie', 'Garantie', 'NOMBRE', 65, NULL, 'mois');
    PERFORM pg_temp.carac(pg_temp.cat('Commerce', 'Électroménager'), 'commerce_electromenager_puissance', 'Puissance', 'NOMBRE', 30, NULL, 'W');
    PERFORM pg_temp.carac(pg_temp.cat('Commerce', 'Électroménager'), 'commerce_electromenager_capacite', 'Capacité', 'NOMBRE', 35, NULL, 'L');
    PERFORM pg_temp.carac(pg_temp.cat('Commerce', 'Électroménager'), 'commerce_electromenager_couleurs', 'Couleurs disponibles', 'MULTI_LISTE', 40, couleurs);
    PERFORM pg_temp.carac(pg_temp.cat('Commerce', 'Électroménager'), 'commerce_electromenager_garantie', 'Garantie', 'NOMBRE', 65, NULL, 'mois');

    -- Alimentation : contenance, origine, conservation
    PERFORM pg_temp.carac(pg_temp.cat('Agroalimentaire'), 'alim_contenance', 'Contenance ou poids', 'TEXTE', 10, NULL, NULL, 'Exemple : 900 g, 1 L, lot de 6');
    PERFORM pg_temp.carac(pg_temp.cat('Agroalimentaire'), 'alim_origine', 'Origine', 'LISTE', 20,
        ARRAY['Sénégal', 'Afrique de l''Ouest', 'Maghreb', 'Europe', 'Asie', 'Autre']);
    PERFORM pg_temp.carac(pg_temp.cat('Agroalimentaire'), 'alim_conservation', 'Conservation', 'LISTE', 30,
        ARRAY['Température ambiante', 'Au frais', 'Surgelé']);
    PERFORM pg_temp.carac(pg_temp.cat('Commerce', 'Alimentation'), 'commerce_alim_contenance', 'Contenance ou poids', 'TEXTE', 10, NULL, NULL, 'Exemple : 900 g, 1 L, lot de 6');
    PERFORM pg_temp.carac(pg_temp.cat('Commerce', 'Alimentation'), 'commerce_alim_origine', 'Origine', 'LISTE', 20,
        ARRAY['Sénégal', 'Afrique de l''Ouest', 'Maghreb', 'Europe', 'Asie', 'Autre']);
    PERFORM pg_temp.carac(pg_temp.cat('Commerce', 'Alimentation'), 'commerce_alim_conservation', 'Conservation', 'LISTE', 30,
        ARRAY['Température ambiante', 'Au frais', 'Surgelé']);
END $$;

-- Les robes avaient leurs propres Couleur, Taille et Matière (16_type_offre_attributs_tags.sql) :
-- avec l'héritage, elles feraient doublon avec « Couleurs disponibles », « Tailles disponibles » et
-- « Matière » de Mode et textile. Elles sont désactivées, seulement si aucune offre ne les utilise.
-- Longueur, Occasion et Style, propres aux robes, restent.
UPDATE attribut a SET actif = FALSE
WHERE a.code IN ('mode_couleur', 'mode_taille', 'mode_matiere') AND a.actif
  AND NOT EXISTS (SELECT 1 FROM offre_attribut oa WHERE oa.id_attribut = a.id_attribut);

-- Pastilles des couleurs (fiche article et formulaire)
UPDATE valeur_attribut_possible v SET code_couleur = c.hex
FROM attribut a, (VALUES ('Noir', '#111111'), ('Blanc', '#FFFFFF'), ('Gris', '#8E8E8E'), ('Beige', '#D9C3A0'),
                         ('Marron', '#6B4226'), ('Bleu', '#1F5FBF'), ('Bleu marine', '#1B2A4A'), ('Bleu ciel', '#87CEEB'),
                         ('Rouge', '#C62828'), ('Bordeaux', '#6D1A2B'), ('Rose', '#F06292'), ('Orange', '#F57C00'),
                         ('Jaune', '#FBC02D'), ('Vert', '#2E7D32'), ('Kaki', '#6B6B3A'), ('Violet', '#6A1B9A'),
                         ('Doré', '#C9A227'), ('Argenté', '#C0C0C0')) AS c(nom, hex)
WHERE v.id_attribut = a.id_attribut AND a.code IN ('mt_couleurs', 'elec_couleurs', 'commerce_electromenager_couleurs') AND v.valeur = c.nom
  AND v.code_couleur IS DISTINCT FROM c.hex;
