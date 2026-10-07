-- ============================================================================
-- 07/10 : lieux publics en double (Assemblée nationale, BCEAO, Cathédrale…).
--
-- Les scripts de lieux ne s'insèrent que si le premier lieu manque, en comparant le nom.
-- Sur une base dont les accents avaient été abîmés (« AssemblÃ©e »), la comparaison échouait :
-- les lieux étaient réinsérés, puis la réparation des accents rendait les deux copies
-- identiques. On garde la plus ancienne copie de chaque lieu (même nom, même commune).
-- Aucune table ne référence lieu_public : la suppression est sans effet de bord.
-- À passer après 02_reparer_accents. Rejouable.
-- ============================================================================

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

DELETE FROM lieu_public l
USING lieu_public garde
WHERE lower(trim(garde.nom)) = lower(trim(l.nom))
  AND lower(coalesce(trim(garde.commune), '')) = lower(coalesce(trim(l.commune), ''))
  AND garde.id_lieu < l.id_lieu;
