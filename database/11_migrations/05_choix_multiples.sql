-- ============================================================================
-- 06/10 : caractéristiques à choix multiples et caractéristiques propres aux produits.
--
-- 1. Tailles, couleurs et pointures se cochent à plusieurs (comme dans DressIT) : un vendeur
--    propose S, M et L, en noir et en blanc. Les anciennes listes à choix unique deviennent des
--    choix multiples, avec des valeurs complétées et la pastille de chaque couleur.
-- 2. attribut.pour_type : une caractéristique de produit (taille, couleur, garantie…) n'apparaît
--    plus sur un service (« Câblage réseau » n'a ni état ni couleur).
-- 3. « État » (neuf, occasion…) doublait la case « Article neuf » du produit : il est retiré.
-- Rejouable.
-- ============================================================================

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

ALTER TABLE attribut ADD COLUMN IF NOT EXISTS pour_type VARCHAR(10) NOT NULL DEFAULT 'TOUS';
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_attribut_pour_type') THEN
        ALTER TABLE attribut ADD CONSTRAINT ck_attribut_pour_type CHECK (pour_type IN ('TOUS', 'PRODUIT', 'SERVICE'));
    END IF;
END $$;

-- 1. Choix multiples (une voiture, elle, n'a qu'une couleur : vehicule_couleur reste un choix unique)
UPDATE attribut SET type_champ = 'MULTI_LISTE',
       nom = CASE WHEN nom ~* '^taille$' THEN 'Tailles disponibles'
                  WHEN nom ~* '^couleur$' THEN 'Couleurs disponibles'
                  WHEN nom ~* '^pointure$' THEN 'Pointures disponibles' ELSE nom END
WHERE type_champ = 'LISTE' AND code <> 'vehicule_couleur' AND nom ~* '^(taille|couleur|pointure)s?( disponibles?)?$';

-- Valeurs complétées (ajoutées si absentes, à la suite)
CREATE OR REPLACE FUNCTION pg_temp.completer(code_ TEXT, valeurs TEXT[]) RETURNS VOID AS $$
DECLARE ida BIGINT; v TEXT; dernier INT;
BEGIN
    SELECT id_attribut INTO ida FROM attribut WHERE code = code_;
    IF ida IS NULL THEN RETURN; END IF;
    FOREACH v IN ARRAY valeurs LOOP
        SELECT COALESCE(MAX(ordre_affichage), 0) INTO dernier FROM valeur_attribut_possible WHERE id_attribut = ida;
        INSERT INTO valeur_attribut_possible (id_attribut, valeur, ordre_affichage, actif)
        SELECT ida, v, dernier + 1, TRUE WHERE NOT EXISTS (SELECT 1 FROM valeur_attribut_possible WHERE id_attribut = ida AND valeur = v);
    END LOOP;
END $$ LANGUAGE plpgsql;

DO $$
DECLARE
    couleurs TEXT[] := ARRAY['Noir', 'Blanc', 'Gris', 'Beige', 'Marron', 'Bleu', 'Bleu marine', 'Bleu ciel', 'Rouge',
                             'Bordeaux', 'Rose', 'Orange', 'Jaune', 'Vert', 'Kaki', 'Violet', 'Doré', 'Argenté', 'Multicolore'];
BEGIN
    PERFORM pg_temp.completer('vetement_taille', ARRAY['XS', 'S', 'M', 'L', 'XL', 'XXL', '3XL']);
    PERFORM pg_temp.completer('vetement_couleur', couleurs);
    PERFORM pg_temp.completer('chaussure_pointure', ARRAY['36', '37', '38', '39', '40', '41', '42', '43', '44', '45', '46']);
    PERFORM pg_temp.completer('mobile_couleur', ARRAY['Noir', 'Blanc', 'Gris', 'Bleu', 'Rose', 'Vert', 'Violet', 'Doré', 'Argenté']);
END $$;

-- Doublons de « Mode et textile » (couleurs, tailles héritées), sans valeurs et jamais utilisés : retirés
UPDATE attribut a SET actif = FALSE
WHERE a.code IN ('bazin_couleur', 'costume_couleur', 'costume_taille') AND a.actif
  AND NOT EXISTS (SELECT 1 FROM offre_attribut oa WHERE oa.id_attribut = a.id_attribut);

-- Pastille de chaque couleur, quelle que soit la caractéristique
UPDATE valeur_attribut_possible v SET code_couleur = c.hex
FROM attribut a, (VALUES ('Noir', '#111111'), ('Blanc', '#FFFFFF'), ('Gris', '#8E8E8E'), ('Beige', '#D9C3A0'),
                         ('Marron', '#6B4226'), ('Bleu', '#1F5FBF'), ('Bleu marine', '#1B2A4A'), ('Bleu ciel', '#87CEEB'),
                         ('Rouge', '#C62828'), ('Bordeaux', '#6D1A2B'), ('Rose', '#F06292'), ('Orange', '#F57C00'),
                         ('Jaune', '#FBC02D'), ('Vert', '#2E7D32'), ('Kaki', '#6B6B3A'), ('Violet', '#6A1B9A'),
                         ('Doré', '#C9A227'), ('Argenté', '#C0C0C0')) AS c(nom, hex)
WHERE v.id_attribut = a.id_attribut AND a.nom ~* 'couleur|coloris' AND v.valeur = c.nom
  AND v.code_couleur IS DISTINCT FROM c.hex;

-- 2. Caractéristiques de produit : tailles, couleurs, matière, pointures, stockage, garantie,
--    contenance, puissance… (les services de ces rayons ne les voient plus)
UPDATE attribut SET pour_type = 'PRODUIT'
WHERE pour_type = 'TOUS' AND (
      code ~ '^(mt_|vet_|chemise_|chau_|enfant_|tel_|info_|elec_|electromenager_|maison_electromenager_|commerce_|alim_|vetement_|chaussure_|mobile_|costume_|bazin_|mode_)'
   OR nom ~* '^(tailles?|couleurs?|pointures?|matière|garantie|stockage|mémoire|ram|contenance|capacité|puissance|marque|modèle|état)( |$)');

-- 3. « État » doublait la case « Article neuf » du produit
UPDATE attribut SET actif = FALSE WHERE code IN ('info_etat', 'elec_etat') AND actif;

-- 4. Types d'offre de service enregistrés comme produits (« Câblage réseau », « Coiffure »,
--    « Réparation téléphone », « Cours de langues », locations…) : le formulaire leur proposait
--    l'état, la garantie et le stock d'un produit. « Autre » suit les autres types de sa catégorie.
UPDATE type_offre SET principale = 'SERVICE'
WHERE actif IS NOT FALSE AND CAST(principale AS TEXT) = 'PRODUIT' AND libelle !~* '^autre' AND libelle !~* 'transformation'
  AND libelle ~* '\m(installation|câblage|cablage|réparation|reparation|maintenance|dépannage|depannage|livraison|cours|formation|coiffure|nettoyage|ménage|location|conseil|soins|couture|retouches?|organisation)';

UPDATE type_offre a SET principale = 'SERVICE'
WHERE a.libelle ~* '^autre' AND CAST(a.principale AS TEXT) = 'PRODUIT'
  AND EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = a.id_categorie AND t.libelle !~* '^autre')
  AND NOT EXISTS (SELECT 1 FROM type_offre t WHERE t.id_categorie = a.id_categorie AND t.libelle !~* '^autre'
                  AND CAST(t.principale AS TEXT) = 'PRODUIT');

-- 5. Ordre naturel des valeurs : XS avant S, 36 avant 37, couleurs dans le même ordre partout
UPDATE valeur_attribut_possible v SET ordre_affichage = array_position(ARRAY['XXS', 'XS', 'S', 'M', 'L', 'XL', 'XXL', '3XL', '4XL'], v.valeur)
FROM attribut a WHERE v.id_attribut = a.id_attribut AND a.nom ~* 'taille'
  AND array_position(ARRAY['XXS', 'XS', 'S', 'M', 'L', 'XL', 'XXL', '3XL', '4XL'], v.valeur) IS NOT NULL;
UPDATE valeur_attribut_possible v SET ordre_affichage = CAST(v.valeur AS INTEGER)
FROM attribut a WHERE v.id_attribut = a.id_attribut AND a.nom ~* 'pointure' AND v.valeur ~ '^[0-9]{1,2}$';
UPDATE valeur_attribut_possible v SET ordre_affichage = array_position(ARRAY['Noir', 'Blanc', 'Gris', 'Beige', 'Marron', 'Bleu',
       'Bleu marine', 'Bleu ciel', 'Rouge', 'Bordeaux', 'Rose', 'Orange', 'Jaune', 'Vert', 'Kaki', 'Violet', 'Doré', 'Argenté', 'Multicolore'], v.valeur)
FROM attribut a WHERE v.id_attribut = a.id_attribut AND a.nom ~* 'couleur'
  AND array_position(ARRAY['Noir', 'Blanc', 'Gris', 'Beige', 'Marron', 'Bleu', 'Bleu marine', 'Bleu ciel', 'Rouge', 'Bordeaux', 'Rose',
       'Orange', 'Jaune', 'Vert', 'Kaki', 'Violet', 'Doré', 'Argenté', 'Multicolore'], v.valeur) IS NOT NULL;
