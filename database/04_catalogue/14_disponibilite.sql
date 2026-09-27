DROP TABLE IF EXISTS disponibilite CASCADE;

CREATE TABLE disponibilite (

    id_disponibilite BIGSERIAL PRIMARY KEY,

    id_offre BIGINT NOT NULL,

    id_ressource BIGINT,

    date_debut TIMESTAMP NOT NULL,

    date_fin TIMESTAMP NOT NULL,

    capacite_totale INTEGER DEFAULT 1,

    capacite_restante INTEGER DEFAULT 1,

    prix_special NUMERIC(12,2),

    est_disponible BOOLEAN DEFAULT TRUE,

    commentaire TEXT,

    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_disponibilite_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)
        ON DELETE CASCADE,

    CONSTRAINT fk_disponibilite_ressource
        FOREIGN KEY(id_ressource)
        REFERENCES ressource(id_ressource)
        ON DELETE CASCADE,

    CONSTRAINT chk_dates
        CHECK (date_fin > date_debut),

    CONSTRAINT chk_capacite
        CHECK (
            capacite_restante >= 0
            AND capacite_restante <= capacite_totale
        )
);