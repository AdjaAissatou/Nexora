DROP TABLE IF EXISTS service CASCADE;

CREATE TABLE service (

    id_service BIGSERIAL PRIMARY KEY,

    id_offre BIGINT UNIQUE NOT NULL,

    duree_estimee INTEGER,

    intervention_domicile BOOLEAN DEFAULT FALSE,

    intervention_distance BOOLEAN DEFAULT FALSE,

    delai_reponse INTEGER,

    reservation BOOLEAN DEFAULT TRUE,

    urgence BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_service_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)

        ON DELETE CASCADE

);