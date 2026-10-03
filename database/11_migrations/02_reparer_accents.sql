-- ============================================================================
-- Répare les accents abîmés par un script SQL lu avec un mauvais encodage
-- (« journÃ©e » au lieu de « journée », « Ã‰ducation » au lieu de « Éducation »).
--
-- Cause : sous Windows, psql lit par défaut les fichiers dans l'encodage de la
-- console (WIN1252) ; un script enregistré en UTF-8 voit alors chaque lettre accentuée
-- enregistrée comme deux caractères. Les scripts commencent désormais par
-- « SET client_encoding = 'UTF8' », mais une base déjà touchée doit être réparée.
--
-- Méthode : chaque valeur de texte contenant ces traces (Ã, Â, â€, Å) est
-- reconvertie (WIN1252 -> UTF-8). Une valeur n'est remplacée que si la conversion
-- réussit et supprime les traces ; sinon elle est laissée telle quelle.
-- Rejouable : une valeur réparée ne contient plus de trace.
-- Utilisé par mise_a_jour.sql ; peut aussi être lancé seul.
-- ============================================================================

SET client_encoding = 'UTF8';

CREATE OR REPLACE FUNCTION pg_temp.reparer_accents(v TEXT) RETURNS TEXT AS $$
DECLARE r TEXT;
BEGIN
    IF v IS NULL OR v !~ '(Ã|Â|â€|Å)' THEN RETURN v; END IF;
    BEGIN
        r := convert_from(convert_to(v, 'WIN1252'), 'UTF8');
    EXCEPTION WHEN OTHERS THEN
        RETURN v;  -- pas un double encodage (ou caractère non convertible) : on ne touche à rien
    END;
    IF r ~ '(Ã|Â|â€)' THEN
        -- encodé deux fois : un second passage
        BEGIN
            r := convert_from(convert_to(r, 'WIN1252'), 'UTF8');
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
    END IF;
    RETURN r;
END $$ LANGUAGE plpgsql;

DO $$
DECLARE c RECORD; n BIGINT; total BIGINT := 0;
BEGIN
    FOR c IN
        SELECT col.table_name, col.column_name
        FROM information_schema.columns col
        JOIN information_schema.tables t ON t.table_schema = col.table_schema AND t.table_name = col.table_name
        WHERE col.table_schema = 'public' AND t.table_type = 'BASE TABLE'
          AND col.data_type IN ('text', 'character varying', 'character')
    LOOP
        EXECUTE format('UPDATE %I SET %I = pg_temp.reparer_accents(%I) WHERE %I ~ ''(Ã|Â|â€|Å)'' AND pg_temp.reparer_accents(%I) IS DISTINCT FROM %I',
                       c.table_name, c.column_name, c.column_name, c.column_name, c.column_name, c.column_name);
        GET DIAGNOSTICS n = ROW_COUNT;
        IF n > 0 THEN
            RAISE NOTICE 'accents réparés : %.% (% ligne(s))', c.table_name, c.column_name, n;
            total := total + n;
        END IF;
    END LOOP;
    RAISE NOTICE 'accents réparés au total : % valeur(s)', total;
END $$;
