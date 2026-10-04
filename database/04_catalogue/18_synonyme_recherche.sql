-- ============================================================================
-- Synonymes de recherche (docs/architecture-acteurs.md §16) : un mot que les clients tapent
-- et ce qu'il couvre dans les offres. « plombier » trouve « Débouchage canalisation ».
-- Les mots sont comparés sans accents et par leur racine (« plombiers » = « plombier »).
-- Créée si absente : la table n'est jamais vidée par une mise à jour.
-- ============================================================================

SET client_encoding = 'UTF8';

CREATE TABLE IF NOT EXISTS synonyme_recherche (
    id_synonyme BIGSERIAL PRIMARY KEY,
    terme VARCHAR(80) NOT NULL,
    equivalent VARCHAR(120) NOT NULL,
    CONSTRAINT uq_synonyme_recherche UNIQUE (terme, equivalent)
);
