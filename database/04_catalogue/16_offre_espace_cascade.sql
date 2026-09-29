-- Permet la suppression d'un espace professionnel même s'il a des offres publiées :
-- sans ON DELETE CASCADE sur offre.id_espace, la suppression échouait avec une violation
-- de contrainte de clé étrangère. Corrige aussi les installations déjà en place.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_offre_espace'
               AND confdeltype <> 'c') THEN
        ALTER TABLE offre DROP CONSTRAINT fk_offre_espace;
        ALTER TABLE offre ADD CONSTRAINT fk_offre_espace
            FOREIGN KEY(id_espace) REFERENCES espace_professionnel(id_espace) ON DELETE CASCADE;
    END IF;
END $$;
