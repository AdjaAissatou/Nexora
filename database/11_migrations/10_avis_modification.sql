-- ============================================================================
-- 09/10 : avis modifiables et réponses des professionnels (docs/architecture-acteurs.md §22).
--
-- L'auteur d'un avis peut le modifier (tant qu'il n'est pas masqué) : la date de modification est
-- affichée (« modifié le … »). La réponse publique du professionnel utilise les colonnes
-- reponse_fournisseur et date_reponse, présentes depuis l'origine mais jamais remplies. Rejouable.
-- ============================================================================

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

ALTER TABLE avis ADD COLUMN IF NOT EXISTS date_modification TIMESTAMP;
COMMENT ON COLUMN avis.date_modification IS 'Dernière modification de la note ou du commentaire par son auteur';
COMMENT ON COLUMN avis.reponse_fournisseur IS 'Réponse publique du professionnel propriétaire de l''espace';
