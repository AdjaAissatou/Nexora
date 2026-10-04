-- ============================================================================
-- Droits du compte des services (nexora_user) sur TOUTES les tables.
--
-- Lancés en « postgres », install.sql et mise_a_jour.sql créent des tables qui
-- appartiennent à postgres : sans ces droits, les services échouent avec
-- « permission denied for table lieu_public » (ou image_vecteur, horaire…).
-- Rejouable ; sans effet si le compte nexora_user n'existe pas.
-- ============================================================================

SET client_encoding = 'UTF8';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'nexora_user') THEN
        GRANT USAGE ON SCHEMA public TO nexora_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO nexora_user;
        GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO nexora_user;
        GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO nexora_user;
        -- Tables créées plus tard par le même compte : droits accordés d'office
        ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO nexora_user;
        ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO nexora_user;
        ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO nexora_user;
    END IF;
END $$;
