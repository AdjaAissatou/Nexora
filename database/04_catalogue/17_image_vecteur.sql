-- ============================================================================
-- Recherche par photo (docs/architecture-acteurs.md §11) : empreinte visuelle de
-- chaque image d'offre, calculée par catalogue-service avec le modèle DINOv2-small.
-- Un vecteur de 384 nombres (float32, 1 536 octets) par URL d'image ; « modele »
-- permet de tout recalculer si le modèle change. Une image illisible garde son
-- erreur, pour ne pas être retentée à chaque recherche.
-- Rejouable (aucune suppression).
-- ============================================================================

CREATE TABLE IF NOT EXISTS image_vecteur (
    url          VARCHAR(500) PRIMARY KEY,
    modele       VARCHAR(60)  NOT NULL,
    vecteur      BYTEA,
    erreur       TEXT,
    date_calcul  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_image_vecteur CHECK (vecteur IS NOT NULL OR erreur IS NOT NULL)
);

-- Comme 09_seed/21_verification.sql : l'utilisateur applicatif, s'il existe, lit et écrit la table.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'nexora_user') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON image_vecteur TO nexora_user;
    END IF;
END $$;
