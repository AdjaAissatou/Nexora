-- ============================================================================
-- Colonnes ajoutées le 28/09 à des tables déjà existantes (bases installées le
-- 27/09). Utilisé par mise_a_jour.sql, rejouable, sans effet sur une base à jour.
-- ============================================================================

-- Catalogue hiérarchique : catégorie parente, unicité du nom par branche.
ALTER TABLE categorie ADD COLUMN IF NOT EXISTS id_categorie_parent BIGINT;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'categorie'::regclass AND conname = 'fk_categorie_parent') THEN
        ALTER TABLE categorie ADD CONSTRAINT fk_categorie_parent
            FOREIGN KEY (id_categorie_parent) REFERENCES categorie(id_categorie) ON DELETE CASCADE;
    END IF;
    -- L'ancien nom UNIQUE global empêche deux sous-catégories de même nom dans deux branches.
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid = 'categorie'::regclass AND contype = 'u'
               AND conkey = ARRAY[(SELECT attnum FROM pg_attribute WHERE attrelid = 'categorie'::regclass AND attname = 'nom')]) THEN
        EXECUTE (SELECT 'ALTER TABLE categorie DROP CONSTRAINT ' || quote_ident(conname)
                 FROM pg_constraint WHERE conrelid = 'categorie'::regclass AND contype = 'u'
                 AND conkey = ARRAY[(SELECT attnum FROM pg_attribute WHERE attrelid = 'categorie'::regclass AND attname = 'nom')]);
    END IF;
END $$;
CREATE UNIQUE INDEX IF NOT EXISTS uq_categorie_parent_nom ON categorie ((COALESCE(id_categorie_parent, 0)), nom);

-- Nature PRODUIT / SERVICE d'un type d'offre.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'type_offre' AND column_name = 'principale') THEN
        ALTER TABLE type_offre ADD COLUMN principale type_offre_principale NOT NULL DEFAULT 'PRODUIT';
        -- Types seedés le 27/09 : leur vraie nature (ils valaient tous PRODUIT par défaut).
        UPDATE type_offre SET principale = 'SERVICE'
        WHERE libelle IN ('Consultations médicales', 'Prestations beauté', 'Transport et livraison',
                          'Formations et cours', 'Services professionnels', 'Hébergement', 'Services administratifs');
    END IF;
END $$;

-- Cycle de vie de l'espace (brouillon, actif, suspendu, fermé…).
ALTER TABLE espace_professionnel ADD COLUMN IF NOT EXISTS statut statut_espace NOT NULL DEFAULT 'ACTIF';
