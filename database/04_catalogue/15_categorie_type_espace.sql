DROP TABLE IF EXISTS categorie_type_espace CASCADE;

-- Rattache chaque domaine racine de la taxonomie aux types d'espace pour lesquels
-- il est pertinent : permet au formulaire "créer une offre" de ne proposer que les
-- domaines qui ont du sens pour le type d'espace du professionnel connecté
-- (ex: un Garage ne voit pas "Mode et textile"). "principal" distingue le domaine
-- le plus évident pour ce type d'espace (affiché en premier).
CREATE TABLE categorie_type_espace (

    id_categorie BIGINT NOT NULL,

    id_type_espace BIGINT NOT NULL,

    principal BOOLEAN NOT NULL DEFAULT FALSE,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_categorie, id_type_espace),

    CONSTRAINT fk_cte_categorie
        FOREIGN KEY(id_categorie)
        REFERENCES categorie(id_categorie)
        ON DELETE CASCADE,

    CONSTRAINT fk_cte_type_espace
        FOREIGN KEY(id_type_espace)
        REFERENCES type_espace(id_type_espace)
        ON DELETE CASCADE

);
