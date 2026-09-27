DROP TABLE IF EXISTS offre_tag CASCADE;

CREATE TABLE offre_tag (

    id_offre BIGINT NOT NULL,

    id_tag BIGINT NOT NULL,

    PRIMARY KEY(id_offre,id_tag),

    CONSTRAINT fk_offretag_offre
        FOREIGN KEY(id_offre)
        REFERENCES offre(id_offre)
        ON DELETE CASCADE,

    CONSTRAINT fk_offretag_tag
        FOREIGN KEY(id_tag)
        REFERENCES tag(id_tag)
        ON DELETE CASCADE

);